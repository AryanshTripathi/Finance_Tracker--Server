## How to check DB

```azure
docker exec -it server-postgres-1 psql -U myuser -d mydatabase -c "\dt"
```

#### DB Query
```azure
\c mydatabase
\dt
```


## Concepts to go through

- How hibernate works and hibernate vs postgres
- How to connect postgres with hibernate
- @GeneratedValue(strategy = GenerationType.UUID) vs leaving generation to the DB and reading it on insert
- Mapping of entities
- jakarta vs javax
- char vs varchar
- Key design decision: skip UserDetailsService/AuthenticationManager


## Rules:
- Each new category response should be enclosed in brackets '()'
  - Example: (Category-1)
  - This will be stored as a new category: Category-1

## TODO — Telegram categorization flow

An incoming Telegram message has no built-in marker saying what it's "about" — the bot has to infer intent. The only unambiguous message type is a button press (`callback_query`), since we control its `callback_data` and can embed the transaction id directly. Free-text replies carry no such reference, so disambiguating them requires correlating the reply to a specific prompt via Telegram's reply-to-message feature (`message.reply_to_message.message_id`) rather than a DLQ/timeout-based guess. Since multiple transactions can need categorization from the same user at once (e.g. several purchases in quick succession), "which transaction is this reply for" must be tracked per-transaction, not per-user — hence the pending-categorization fields live on `Transaction`, not `User`. A separate scheduled job (not the polling loop) handles the case where the user never replies at all, since "time passed with no message" can't be detected from inside a loop that only runs when an update exists.

### Bugs to fix
- [ ] `TelegramClient`: `@Value("${telegram.bot_token")` is missing the closing `}` — malformed property placeholder, will fail at startup.

### Data model changes
- [ ] Add `findByTelegramChatId(Long chatId)` to `UserRepository` — needed to resolve an incoming update's `chat.id` to a `User`.
- [ ] Add fields to `Transaction` (not `User` — a user can have multiple transactions pending categorization at once):
  - [ ] `categorizationPromptMessageId` (Long) — the Telegram `message_id` of the prompt sent for this transaction; used to resolve a free-text reply back to the right transaction via `message.reply_to_message.message_id`.
  - [ ] `categorizationRequestedAt` (Instant) — when the prompt was (last) sent; drives the timeout check.
  - [ ] `categorizationAttempts` (int) — how many times we've re-prompted; drives the give-up threshold.
  - [ ] DB migration for the above three columns.
  - [ ] Decide the terminal "gave up" representation — new `CategorizationStatus` enum value (e.g. `UNCATEGORIZED_ABANDONED`) vs a separate flag.

### `handleBotUpdate` — actual dispatch logic
- [ ] Loop over `updates.result()`, branch on `callbackQuery` vs `message` present.
- [ ] `/start` command: look up `User` by `chat.id`.
  - [ ] Not found → generate 6-digit code, store in `telegram_link_request`, reply with it.
  - [ ] Found → reply "bot already started."
- [ ] `callback_query` (button press, existing-category selection): `callback_data` embeds the transaction id + category directly — no ambiguity possible even with multiple transactions pending. Update that transaction's category straight from the callback.
- [ ] Free-text reply (custom category): read `message.reply_to_message.message_id`, look up the `Transaction` by `categorizationPromptMessageId`, parse the bracket-enclosed category name per the Rule above, create/find the `Category`, update the transaction, clear its pending fields.
- [ ] Anything else (no reply-to-message match, not `/start`, not a recognized callback) → treat as garbage, ignore.
- [ ] Return the correct new offset = `(highest update_id in this batch) + 1` from `handleBotUpdate` (currently hardcoded to `1L`).

### Sending the categorization prompt
- [ ] When a transaction needs categorization, build an inline keyboard: existing categories as buttons (`callback_data` carrying transaction id + category) plus a "Custom" option.
- [ ] After `telegramClient.sendMessage(...)`, read the returned `message_id` out of the response and persist it onto that transaction's `categorizationPromptMessageId`; set `categorizationRequestedAt = now`.

### Retry / escalation for forgotten/malformed categorization — now planned as a new Service C (deferred)
- [ ] **Deferred** — current focus is just the Gmail service (A) and the Telegram service (B). Revisit C only once both of those are built and working.
- [ ] Decided: this becomes its own standalone service (Service C), not a scheduled job inside B. A = Gmail ingestion → publishes new transactions to Q1. B = consumes Q1, owns the Telegram conversation (prompts + button/reply handling). C = polls every ~12h for `Transaction` rows stuck pending (no reply, or malformed reply) and republishes them toward B via new queue(s), rather than B scanning its own DB on a timer.
- [ ] Open — race condition: nothing yet stops B (handling a just-arrived late reply) and C (scanning that same transaction as "stale" in the same window) from acting on one transaction concurrently. Needs optimistic locking or an atomic claim on the row before this is safe.
- [ ] Open — missing third outcome: the design so far only has 2 output queues (forgotten, malformed), but there are 3 real outcomes — forgotten+retry, malformed+retry, and exhausted-retries+give-up (final notice telling the user to categorize via the app instead). Need a 3rd queue/message type, or a `terminal: true` flag on the existing two.
- [ ] Open — decide whether forgotten vs malformed truly need separate queues, or one queue with a `reason` field is enough; splitting only earns its keep if tracking malformed-response rate separately as a UX signal matters.
- [ ] Open — don't conflate broker-level redelivery (consumer crash → DLX) with business-level retry (user didn't respond → re-prompt); these need distinct failure paths across both A→Q1→B and the new C→B queues.
- [ ] Open — fixed timeout per retry, or escalating backoff (e.g. 10 min → 1 hr → give up)?

### Still open / deferred
- [ ] Transactional outbox vs accept-dual-write-risk for the Gmail service's DB-write + RabbitMQ-publish step — discussed at length, not yet decided.
- [ ] `GmailPollingService` and the RabbitMQ exchange/queue/DLX topology between the two services — not yet built.
- [ ] Web UI (or API-only) flow for entering the Telegram link code — not yet decided/built.
- [ ] Service C (forgotten/malformed reminder service) — see section above; parked until A and B are done.
