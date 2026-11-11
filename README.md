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

### Retry / escalation cleanup job
- [ ] New `@Scheduled` job, separate cadence from the polling job (not every 5s — e.g. hourly), scanning `Transaction` rows with `categorizationStatus = UNCATEGORIZED` and `categorizationRequestedAt` older than the timeout.
- [ ] If `categorizationAttempts < maxRetries` → re-send the prompt, bump `attempts`, reset `categorizationRequestedAt`.
- [ ] If `categorizationAttempts >= maxRetries` → clear pending fields, flip to the terminal "abandoned" status, optionally notify the user.
- [ ] Decide: fixed timeout per retry, or escalating backoff (e.g. 10 min → 1 hr → give up)? — still open.

### Still open / deferred
- [ ] Transactional outbox vs accept-dual-write-risk for the Gmail service's DB-write + RabbitMQ-publish step — discussed at length, not yet decided.
- [ ] `GmailPollingService` and the RabbitMQ exchange/queue/DLX topology between the two services — not yet built.
- [ ] Web UI (or API-only) flow for entering the Telegram link code — not yet decided/built.
