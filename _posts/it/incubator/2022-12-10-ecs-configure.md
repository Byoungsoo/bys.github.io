---
layout: post
title: "AWS ECS환경 구축하기"
author: "Bys"
category: container
date: 2023-11-21 01:00:00
tags: ecs aws
---

# [ECS]()


## 1. ECS환경 구성하기 

1. Launch Template

`InstanceRole`  
```yaml
```

`Userdata`  
```bash
#!/bin/bash
cat <<'EOF' >> /etc/ecs/ecs.config
ECS_CLUSTER=bys-dev-ecs-main
EOF
```



2. AutoScaling 그룹

3. ECS 클러스터 생성




<br><br><br>

> Ref: https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/instance_IAM_role.html