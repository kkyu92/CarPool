# CarPool - 카풀
* * *
<p align="center">
    <img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/drawable/carpool.png" alt="liveAuction logo" width="400" height="300"></p>
<div style="text-align: center">출발지와 도착지가 비슷한 운전자와 탑승자들이 카풀을 할 수 있는 매칭 서비스입니다.</div>

<center>장소검색, 경로저장, 조건검색, 소요시간과 요금, 채팅, 영상통화, 위치공유</center>


## 목차

- [사용기술](#사용기술)
- [핵심기능](#핵심기능)
- [오픈소스](#오픈소스)
- [참고사항](#참고사항)


## 사용기술

언어
- Java
- Php

운영체제
- Android
- Linux(ubuntu)

서버
- Apache

데이터베이스
- MySQL

라이브러리 & API
- glide
- times-square
- retrofit2
- kakao-login
- facebook-login
- google-login
- google-map
- Tmap

## 핵심기능
- [소셜로그인](#소셜로그인)
- [카풀요청-탑승자](#카풀요청-탑승자)
- [운행경로-운전자](#운행경로-운전자)
- [카풀목록-운전자](#카풀목록-운전자)
- [채팅](#채팅)
- [영상통화](#영상통화)
- [위치공유](#위치공유)
- [결제,평가](#결제,평가)


### 소셜로그인
- - -

- Facebook
- Google
- Kakao

### 카풀요청-탑승자
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/request.gif" height="550"></img>
- Tmap API를 이용해 출발지와 도착지 장소검색
- Google map에 경로를 Polyline으로 표시
- 소요시간, 이동거리, 요금 표시
- 출발 일자와 시간을 설정 
  (이전시간 선택불가)
- 탑승인원 수 설정


### 운행경로-운전자
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/driverRouteSet.gif" height="550"></img>
- 운전자는 자신의 운행경로를 등록하여, 경로가 비슷한 카풀요청을 찾을 수 있다
- Google map 화면에 터치를 통해 출발지와 도착지 마커를 표시
- 경로 등록, 수정, 삭제 가능
- Google map 스냅샷을 사용해 등록한 경로에 대한 지도 이미지 저장

### 카풀목록-운전자
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/driverCarpoolList.gif" height="550"></img>
- 모든 카풀 요청을 보여주며, 우측 상단의 설정 버튼을 통해 원하는 요청정보 설정 가능
- 날짜, 시간, 탑승인원, 출발 도착지와의 거리 설정
- 요청정보를 클릭하여 지도와 함께 상세 정보를 확인 가능
  (요청한 탑승자의 평점, 운행경로와 요청경로 비교, 등록한 출발지로부터 소요시간, 도착예상시간)

### 채팅
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/chat.gif" height="550"></img>
- 카풀 매칭이 완료된 운전자와 탑승자는 채팅이 가능
-
-

### 영상통화
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/faceCall.gif" height="550"></img>
- 
-
- 

### 위치공유 이동
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/move.gif" height="550"></img>
- 상대방의 위치를 공유하고 출발지까지의 예상도착 알림을 띄워준다
  (5분, 3분, 곧 도착합니다)
- 차량 탑승 확인 여부를 묻고 안심메시지를 보낸다
  (카톡, 문자)

### 결제,평가
- - -
<img src="https://raw.githubusercontent.com/kkyu92/CarPool/master/app/src/main/res/gif/payRating.gif" height="550"></img>
- 목적지에 도착하게 되면 결제알림과 버튼 생성
- incis 사용
- 결제 후 상대방에 대한 평가
  (카풀 매칭시 나오는 평점에 영향)



## 오픈소스

 
## 참고사항
