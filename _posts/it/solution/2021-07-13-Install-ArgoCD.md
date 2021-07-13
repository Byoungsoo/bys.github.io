---
layout: post
title: "Install Argo CD"
author: "Bys"
category: solution
date: 2021-04-21 01:00:00
tags: cicd gitlab gitlab-runner pipeline docker docker-compose
---


#### - Argo CD  
> Argo CD is a declarative, GitOps continuous delivery tool for Kubernetes  


#### - Kubernets Helm을 통한 설치

`Create Namespace`  
```bash
kubectl create namespace argo
```   

`Donwload Template`  
```bash
helm fetch argo/argo-cd
tar -xvzf argo-cd-3.6.4.tgz
```   

`Modify values.yaml`  
```bash
cd argo-cd
vim values.yaml
```   

`Service Using NLB`  
```yaml
## Server service configuration
  service:
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
      service.beta.kubernetes.io/aws-load-balancer-subnets: subnet-0745ae9d00efcb688, subnet-070711a376c168a6e
    labels: {}
    type: LoadBalancer
    ## For node port default ports
    nodePortHttp: 30080
    nodePortHttps: 30443
    servicePortHttp: 80
    servicePortHttps: 443
    servicePortHttpName: http
    servicePortHttpsName: https
    namedTargetPort: true
    loadBalancerIP: ""
    loadBalancerSourceRanges: []
    externalIPs: []
    externalTrafficPolicy: ""
```   

`Service Using NLB`  
```bash
helm install argo -n argo argo/argo-cd -f values.yaml
```  

helm을 통해 정상 배포 후에는 아래와 같이 argo-argocd가 배포되며 argo-argocd-server의 EXTERNAL-IP를 통해 접속 할 수 있다.  
```bash
 k get po -n argo
NAME                                                  READY   STATUS    RESTARTS   AGE
argo-argocd-application-controller-7cdcc48d5c-jnt8k   1/1     Running   0          62m
argo-argocd-dex-server-fd6b7d5fc-ssnbp                1/1     Running   0          62m
argo-argocd-redis-7c9dc5d5f4-lczkd                    1/1     Running   0          62m
argo-argocd-repo-server-765d5cdbb-qgsln               1/1     Running   0          62m
argo-argocd-server-74c959d6dc-vbtsb                   1/1     Running   0          62m

k get svc -n argo
NAME                                 TYPE           CLUSTER-IP       EXTERNAL-IP                                                                          PORT(S)                      AGE
argo-argocd-application-controller   ClusterIP      172.20.113.195   <none>                                                                               8082/TCP                     62m
argo-argocd-dex-server               ClusterIP      172.20.20.74     <none>                                                                               5556/TCP,5557/TCP            62m
argo-argocd-redis                    ClusterIP      172.20.72.203    <none>                                                                               6379/TCP                     62m
argo-argocd-repo-server              ClusterIP      172.20.90.9      <none>                                                                               8081/TCP                     62m
argo-argocd-server                   LoadBalancer   172.20.199.156   a365a14fe8cfd40d3905ff4ee69d52e0-**.elb.ap-northeast-2.amazonaws.com                 80:31017/TCP,443:31146/TCP   62m
```



<br>

#### - Install Docker  
```bash
sudo yum install docker
# cicdadm계정으로 docker 사용
sudo usermod -aG docker cicdadm
```
<br>