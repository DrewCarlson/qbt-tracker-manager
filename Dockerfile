FROM ubuntu:noble

WORKDIR /app

COPY build/bin/linuxX64/releaseExecutable/qbt-tracker-manager.kexe ./qbt-tracker-manager-x64
COPY build/bin/linuxArm64/releaseExecutable/qbt-tracker-manager.kexe ./qbt-tracker-manager-arm64

RUN if [ "$(uname -m)" = "x86_64" ]; then mv /app/qbt-tracker-manager-x64 /app/qbt-tracker-manager; else mv /app/qbt-tracker-manager-arm64 /app/qbt-tracker-manager; fi

RUN chmod u+x qbt-tracker-manager

ENV QBT_URL="http://localhost:8080"
ENV QBT_USERNAME="admin"
ENV QBT_PASSWORD="adminadmin"
ENV ADD_LIST_FILE="/app/addlist.txt"
ENV BAN_LIST_FILE="/app/banlist.txt"
ENV SYNC_INTERVAL="10"

ENTRYPOINT sh -c './qbt-tracker-manager --url "$QBT_URL" --username "$QBT_USERNAME" --password "$QBT_PASSWORD" --add-list-file "$ADD_LIST_FILE" --ban-list-file "$BAN_LIST_FILE" --sync-interval "$SYNC_INTERVAL"'