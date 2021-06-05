---
layout: post
title: "git pull - git 사용법#3"
author: "Bys"
category: git
date: 2021-02-11 01:00:00
tags: git gitcommand
---

pull의 명령어는 원격저장소의 변경된 데이터를 로컬에 반영할 수 있다.

`git pull`

로컬데이터와 충돌이 없을 경우 변경된 데이터를 로컬에 반영할 때 (Fast-Foward)
각 Branch에 맞게 아래와 같이 사용 할 수 있다.

```git
git pull origin master
```
```git
git pull origin develop
```



일반적으로 변경된 데이터를 로컬에 반영하려고 git pull을 사용하였으나 로컬 데이터에도 변경이 발생하여 충돌이 발생한 경우는 충돌 된 부분을 수작업으로 반영하고 merge를 해주어야 한다.

다만, 원격저장소의 데이터를 기준으로 로컬 데이터를 강제로 덮어쓰고 싶다면 아래의 명령을 수행 하면 된다.

`git pull (overwrite force)`
```git
git fetch --all 
git reset --hard origin/master 
git pull origin master  
```
이렇게 명령을 수행하게 되면 원격저장소(origin)의 master를 기준으로 로컬데이터를 엎어쓰게 된다. 

<br><br> 
