#!/bin/bash

###############
# CONFIGURATION
###############

# Your Inapp Billing Public Key. This is NECESSARY if you want the shop
# to work.
#
# See: http://developer.android.com/google/play/billing/billing_integrate.html#billing-security
# Modifies: ShopActivity.java in src/net/globide/coloring_book_08/
BILLING_PUBLIC_KEY=""

######################
# SYSTEM CONFIGURATION
######################

# Don't modify this unless you are moving this file.
PROJECT_DIR="."
# Extension sed uses for backups during substitution.
BACKUP_EXT=".bu"

###########
# FUNCTIONS
###########

function validate_user {
    printf "
    DO NOT PROCEED IF YOU HAVE NOT SET UP THIS SCRIPT'S CONFIG VARIABLES!

    This script is meant to be executed only once, right after a git clone
    (of the master AND submodule repositories). Run this script from the
    root of the project directory.

    It will customize the repository so that the application can compile
    properly, using your own IDs for various components. If you do not set
    up these components (either by running this script or through manual
    configuration of the necessary files), the application will NOT
    run properly. It may not even compile.

    To setup the necessary config variables, open build.sh in a text editor
    and only modify the CONFIGURATION section. Do not modify \$PROJECT_DIR,
    unless you are moving the build.sh file.\n\n"

  input=""
  printf "Are you ready to run the build (y/n)? "
  read input
  printf "\n"
  if [ "$input" != "y" ]; then
    message="Build aborted!"
    printf "\e[31m$message\e[0m\n"
    exit 1
  fi
}

function check_sanity {
  isInvalid=false
  if [ -z "$BILLING_PUBLIC_KEY" ]; then
    isInvalid=true
    message="Error: You must set the \$BILLING_PUBLIC_KEY variable"
    printf "\e[31m$message\e[0m\n"
  fi
  if $isInvalid; then
    message="Build aborted due to errors!"
    printf "\e[31m$message\e[0m\n"
    exit 2
  fi
}

function prepare_ShopActivity {
  target="$PROJECT_DIR/src/net/globide/coloring_book_08/ShopActivity.java"
  sub_string="s|String base64EncodedPublicKey = getKey();|String base64EncodedPublicKey = \"$BILLING_PUBLIC_KEY\";|g"
  sed_file "$target" "$sub_string"
}

function update_android_project {
  android update project --path $PROJECT_DIR
}

function sed_file {
  file=$1
  sub_string=$2

  sed -i$BACKUP_EXT "$sub_string" $file
  exit_code=$?

  if [ $exit_code != 0 ]; then
    message="Failed to run the build on file \"$file\"."
    printf "\e[31m$message\e[0m\n"
  else
    rm $file$BACKUP_EXT
  fi
}

######
# MAIN
######

case "$1" in
all)
  validate_user
  check_sanity
  prepare_ShopActivity
  update_android_project
  message="Build complete!"
  printf "\e[32m$message\e[0m\n"
  ;;
*)
  printf "Usage: `basename $0` [option]\n"
  printf "Available options:\n"
  for option in all
  do 
      printf "  - $option\n"
  done
  ;;
esac
