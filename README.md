# EthioFlow

Offline-first Android productivity app built around the **Ethiopian calendar**.

Inspired by the best Notion templates — modern dark-navy UI, shared data engine, zero setup.

## All stages complete

| Stage | Module | Status |
|-------|--------|--------|
| 1 | Core + Tasks / Projects | ✅ |
| 2 | Habits (streaks, daily check-in) | ✅ |
| 3 | Notes / Second brain (PARA + Inbox) | ✅ |
| 4 | Goals (link tasks & habits via EntityLink) | ✅ |
| 5 | Finance (income / expense, ETB) | ✅ |
| 6 | Journal + Weekly review | ✅ |

## Screens

- **Today** – greeting, habits due, today’s tasks, stats
- **Calendar** – full Ethiopian month (13 months + Pagume), add tasks by day
- **Habits** – create, check-in, streaks
- **More** hub → Projects · Notes · Goals · Finance · Journal

### Notes (PARA)
Inbox → quick capture → move to Projects / Areas / Resources / Archive

### Goals
Create goals, refresh progress from linked tasks & habits (`EntityLink`)

### Finance
Balance card, income/expense list, categories, Ethiopian dates

### Journal + Weekly Review
Daily mood + entry · auto-pull of completed tasks, habit logs, transactions for the last 7 Ethiopian days

## Shared engine

Entities: Task, Project, Habit, HabitLog, Note, Goal, Transaction, JournalEntry  
+ generic `EntityLink` table (any ↔ any)

Pure-Kotlin Ethiopian calendar · Room offline DB · AlarmManager reminders

## Build

```bash
gradle :app:assembleDebug
```

Or push to GitHub — workflow uploads `ethioflow-debug-apk`.

## Sideloading

Android 13+: App info → ⋮ → Allow restricted settings (if notifications blocked).
