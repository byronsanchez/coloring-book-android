#!/bin/bash

######################
# SYSTEM CONFIGURATION
######################

# Don't modify this unless you are moving this file.
PROJECT_DIR="."
# Don't modify, unless you are customizing this script for your own
# purposes.
NUKE[0]="$PROJECT_DIR/res/drawable*/logo.png"
NUKE[1]="$PROJECT_DIR/ic_launcher-web.png"
NUKE[2]="$PROJECT_DIR/res/drawable*/ic_launcher.png"
NUKE[3]="$PROJECT_DIR/res/drawable*/coloring_book_credits.png"

###########
# FUNCTIONS
###########

function validate_user {
    printf "
    This script is meant to be executed in order to comply with the
    licenses of the project. Information can be found in the COPYRIGHT
    and README files located in the root of the project directory. In
    short, by running the nuke script, you will remove all copyrighted
    materials under which no license of use has been provided. Be careful.
    This may break functionality, so do this when you have replacement
    assets.
    
    To print a list of files to be nuked without actually nuking them,
    run:

      assets/_scripts/nuke.sh list\n\n"

  input=""
  printf "Are you ready to run the nuke (y/n)? "
  read input
  printf "\n"
  if [ "$input" != "y" ]; then
    message="Nuke aborted!"
    printf "\e[31m$message\e[0m\n"
    exit 1
  fi
}

function run_nuke {
  target=$1

  # Check if the target is directory or file.
  if [ -d "$target" ]; then 
    # Directory!
    if [ -L "$target" ]; then
      # It is a symlink!
      rm $target
    else
      # It's a directory!
      rm -rf $target
    fi
  else
    # It's a file!
    rm $target
  fi
}

function nuke_list {
  for key in "${!NUKE[@]}"
  do
    printf "${NUKE[$key]}\n"
  done
}

######
# MAIN
######

case "$1" in
all)
  validate_user
  for key in "${!NUKE[@]}"
  do
    printf "Nuking ${NUKE[$key]}...\n"
    run_nuke "${NUKE[$key]}"
  done
  printf "\n"
  message="Nuke complete!"
  printf "\e[32m$message\e[0m\n"
  ;;
list)
  nuke_list
  ;;
*)
  printf "Usage: `basename $0` [option]\n"
  printf "Available options:\n"
  for option in all list
  do 
    printf "  - $option\n"
  done
  ;;
esac
