#!/usr/bin/env bash
ps aux|grep java|grep financing|grep -v grep|awk '{print $2}'|xargs kill -9