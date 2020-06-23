# Network-Programming

1. SSL ->
for server and client :
1)"자기경로"\pingpong\bin > mkdir .keystore<br>
2) keytool -genkey -alias Server -keyalg RSA -v -storetype JKS -keystore .keystore\ServerKey<br>
3) 저장소 : 123456 / 키 : 123456<br>
for client : <br>
4) keytool -export -keystore .keystore\ServerKey -alias Server -file .keystore\ServerKey.cert<br>
5) copy .keystore\ServerKey.cert<br>
6) keytool -import -keystore trustedcerts -alias Server -file ServerKey.cert<br>
7) 저장소: 123456<br>
실행<br>
8) java pingpong.Server / java pingpong.Main<br>
