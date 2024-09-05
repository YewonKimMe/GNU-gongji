FROM ubuntu:latest
LABEL authors="mimms"

ENTRYPOINT ["top", "-b"]