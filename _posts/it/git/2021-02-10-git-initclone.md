---
layout: post
title: "git clone, git init - git 사용법#2"
author: "Bys"
category: git
date: 2021-02-10 11:00:00
tags: git gitcommand
---

git을 시작하며 git 레파지토리의 프로젝트를 복사해오거나 로컬의 프로젝트를 git레파지토리로 업로드 할 때 사용한다.

`git clone`

github에서 특정 project1을 나의 로컬로 복사해오고 싶다면 아래와 같이 명령을 수행한다.

```git
cd D:\dev\Workspace\project1 
git clone https://github.com/USERNAME/project1.git
```

`git init`

나의 로컬 project1을 github로 push하고 싶다면 github에서 레파지토리를 만들고 아래의 명령을 수행한다.

```git
cd D:\dev\Workspace\project1
git init
git add --all
git commit -m "Initial Commit" 
git remote add origin https://github.com/USERNAME/project1.git 
git push -ur origin master 
```

<br><br>