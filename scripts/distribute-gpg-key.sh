#!/bin/bash

key_id="$(gpg --list-keys | sed -n 4p | xargs)"

#gpg --keyserver keyserver.ubuntu.com --send-keys "$key_id"
#gpg --keyserver keys.openpgp.org --send-keys "$key_id"
#gpg --keyserver pgp.mit.edu --send-keys "$key_id"

echo "$key_id"

echo 1-1 | gpg --keyserver keyserver.ubuntu.com --search-key "$key_id"
