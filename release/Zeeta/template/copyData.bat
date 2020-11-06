@echo ----------------------------------------------------
@echo 自ZeetaのDBを初期化して他のZeetaのDBをコピーするサンプルです。
@echo この例では、postgreSQLのDBを自DBにコピーします。
@echo 実行には、postgreSQLのJDBCドライバがカレントフォルダに必要です。
@echo よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@set CLS=../lib/selj.jar
@pause
@del /Q db\*.*
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@java -cp %CLS% ^
-DsrcDb.driver=org.postgresql.Driver ^
-DsrcDb.url=jdbc:postgresql://localhost:5432/zeeta ^
-DsrcDb.user=zeeta ^
-DsrcDb.password=zeeta ^
jp.tokyo.selj.util.CopyData 
@pause
