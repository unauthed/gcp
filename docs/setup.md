
# Contribute Code

First you need to read and accept our [CONTRIBUTING.md](code-of-conduct.md) requirements. This basically says that you wave your copyright for the contributed code and documentation.

https://guides.github.com/activities/forking/

## Fork our respository in GitHub

```
git clone git@github.com:YOUR_ACCOUNT/gdg-bristol.git

cd gdg-bristol

git remote -v

git remote add upstream git@github.com:unauthed/gdg-bristol.git

git fetch upstream

git checkout origin/master

git rebase upstream/master

git push -f origin master

or

git merge upstream/master

git push origin master

```

----

## GCP configure Mac OS using HomeBrew

```
Install Homebrew:
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

brew cask install caskroom/versions/java8
brew install maven
brew cask install google-cloud-sdk
```
