# Network-Programming

1. SSL ->
	for server and client :
	1)"자기경로"\pingpong\bin > mkdir .keystore
	2) keytool -genkey -alias Server -keyalg RSA -v -storetype JKS -keystore .keystore\ServerKey
	3) 저장소 : 123456 / 키 : 123456
	for client : 
	4) keytool -export -keystore .keystore\ServerKey -alias Server -file .keystore\ServerKey.cert
	5) copy .keystore\ServerKey.cert
	6) keytool -import -keystore trustedcerts -alias Server -file ServerKey.cert
	7) 저장소: 123456
	실행
	8) java pingpong.Server / java pingpong.Main
