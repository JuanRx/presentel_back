### Configuración de DataSource

1. descargar el driver correspondiente a la base de datos(en este caso postgresql-42.2.9) y agregarlo a directorio c:\temp
	`<link>` : <https://dev.mysql.com/downloads/file/?id=504236>

2. entrar a la carpeta del servidor web(para este caso wildfly)
	`cd C:\Users\afran\wildfly-16.0.0.Final\bin`
	
3. iniciar conexion: 
	`jboss-cli.bat --connect controller=127.0.0.1`
	
5. crear un controlador
	`deploy C:\Temp\mysql-connector-java-5.1.17-bin.jar`

6.  agregar datasource con las credenciales correspondientes
	`data-source add --name=etec-ds --jndi-name=java:/etec-ds --driver-name=mysql-connector-java-5.1.17-bin.jar --connection-url=jdbc:mysql://192.168.3.40:3306/etecbd_prod --user-name=etec --password=123456`
	
7. verificar los datasource existentes
	`/subsystem=datasources:read-resource`

8. probar la conexion
	`/subsystem=datasources/data-source="etec-ds":test-connection-in-pool`
