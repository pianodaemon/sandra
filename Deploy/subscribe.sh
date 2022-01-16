#!/bin/bash -x

# XXX: Before run this
# Don't forget to set AWS_PROFILE with the respective entitled profile

# Deploys the subscriptor stack
__deploy_stack() {

        local temp="subscriptor_stack.yaml"
        local deploy_cmd=$(printf 'aws cloudformation deploy --stack-name %s --template-file %s  --capabilities CAPABILITY_NAMED_IAM' "${1}" "${temp}")

        # Verification of presence
        if [[ ! -f $temp ]]; then
                echo "Cloudformation template not found"
                exit 1
        fi

        # Verification of content
        if [[ ! -s $temp ]]; then
                echo "Emptyness at Cloudformation template's content"
                exit 1
        fi

        $deploy_cmd
}

__deploy_stack $1
