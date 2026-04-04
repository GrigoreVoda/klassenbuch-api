FROM ubuntu:latest
LABEL authors="grigore"

ENTRYPOINT ["top", "-b"]