# Inside the Kafka Broker: How Messages are Stored

You asked about "Logic and Theory" and "Inside Broker". This document dives deep into the **physical files** on your connection.

Based on your `server.properties`, your logs are configured here:
> `log.dirs=/tmp/kafka-logs`

On your Windows system (running as drive `W:`), this likely translates to:
`W:\tmp\kafka-logs`

---

## 1. The Topic-Partition Directory
Kafka does not have a single "database file". Instead, it uses the **filesystem**.
If you send a message to the topic `inventory.commands`, Kafka creates a directory:

`W:\tmp\kafka-logs\inventory.commands-0`

*   `inventory.commands` = Topic Name
*   `0` = Partition Number (defined by `num.partitions=1` in your config)

---

## 2. The Segment Files (The "Log")
Inside that directory, you won't see just one file. You will see **Segments**.
Kafka splits data into manageable chunks (Segments) so it can easily delete old data (retention).

The files look like this:
```
00000000000000000000.log
00000000000000000000.index
00000000000000000000.timeindex
```

### A. The `.log` File (The Data)
This is the **actual message content**.
*   **Format:** It is a binary stream of messages.
*   **Content:** Each message contains:
    *   Key (size + bytes)
    *   Value (size + bytes -> Your JSON `ReserveStockCommand`)
    *   Header (metadata, timestamp, etc.)
*   **Append Only:** Kafka only ever *adds* to the end of this file. It never updates or deletes from the middle (which is why it's so fast).

### B. The `.index` File (The Lookup)
This is a **sparse index** that maps **Offsets** to **Byte Positions** in the `.log` file.
*   **Logic:** "I want Offset #42."
*   **Broker:** "Looking at `.index`... Offset #42 starts at byte 10240 in the `.log` file."
*   **Benefit:** Allows the consumer to jump directly to a specific message without reading the whole file.

### C. The `.timeindex` File (Time Lookup)
Maps **Timestamps** to **Offsets**.
*   **Logic:** "I want messages from 10 minutes ago."
*   **Broker:** Uses this file to find the Offset that corresponds to that time.

---

## 3. Rolling Segments
Your config says:
`log.segment.bytes=1073741824` (1 GB)

1.  Kafka writes to `00000000000000000000.log`.
2.  Once that file reaches **1 GB**, it closes it (makes it read-only).
3.  It creates a **new** file named after the *next* offset, e.g., `00000000000000050000.log`.

This is how Kafka manages huge amounts of data efficiently.
