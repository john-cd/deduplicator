#! /bin/bash

find somedir -type f -exec md5sum {} \; | sort -k 2
