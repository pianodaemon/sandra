#!/bin/bash -x

DYNDB_UPSTREAM="https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz"

__provision_dyndb() {

	local tmp_dir=$(mktemp -d -t dyndb-XXXXXXXXXX)

	if [[ ! -o pipefail ]]; then
		echo "fail on pipes may not be trustworthy"
		exit 1
	fi

	wget -qO- $DYNDB_UPSTREAM | tar xvz -f - -C $tmp_dir
	if [[ $? != 0 ]]; then
		echo "no dynamo db database provisioned"
		exit 1
	fi

	echo $tmp_dir
}

{
	PIPEFAIL_OLDSTATE="$(shopt -po pipefail)"
	set -o pipefail

	DYNDB_DIR=$(__provision_dyndb)
	if [[ $? != 0 ]]; then
		printf 'FAILURE: %s\n' "${DYNDB_DIR}"
		exit 1
	fi

	printf 'Destroying %s\n' "${DYNDB_DIR}"
	rm -rf $TMP_DIR

	set -vx
	eval "$PIPEFAIL_OLDSTATE"
}
