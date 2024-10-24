# qbt-tracker-manager

Automatically add and remove trackers from torrents in qBittorrent.

## Usage

Create a `banlist.txt` and/or `addlist.txt` file, with each tracker separated by a newline.
Only one list is required for the program to run.

```
udp://tracker1.sometracker.com:1111/announce
udp://tracker2.sometracker.com:1111/announce
```

Run the container with docker-cli or docker-compose:

```yaml
  qbt-tracker-manager:
    image: ghcr.io/drewcarlson/qbt-tracker-manager:latest
    environment:
      QBT_URL: "http://localhost:8080"
      QBT_USERNAME: "admin"
      QBT_PASSWORD: "adminadmin"
      SYNC_INTERVAL: "10" # seconds
    volumes:
      - /path/to/banlist.txt:/app/banlist.txt
      - /path/to/addlist.txt:/app/addlist.txt
```

All `environment` variables listed are the defaults, only provide what is needed.
You can also configure the list paths if desired:
```yaml
ADD_LIST_FILE: "/app/addlist.txt"
BAN_LIST_FILE: "/app/banlist.txt"
```

## License

MIT, see [LICENSE](LICENSE).