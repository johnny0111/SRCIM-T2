# README #

This README documents the steps required for pushing code onto Bitbucket using git.

### PUSH TO A BRANCH ### 

* ON MASTER 
* MVP -> __git status__ ,checks differences between code versions <- MVP Obrigatorio para nao dar overwrite random
* __git pull
 __, to get the master branch from the cloud
* __git checkout -b "new-branch-name"__, creates a new branch

* ON NEW BRANCH
* __git add .__ , prepares files to be commited
* __git commit -m "commit-message"__ , saves changes locally
* __git push --set-upstream origin new-branch-name__ , uploads local changes (commits) to the bitbucket cloud
* __git checkout master__, after the pull request has been accepted

* create pull request no bit buckets. Approve and Merge

* ON MASTER
* __git pull__, to get the modifications previously done on the branch //only after Other person accepts it