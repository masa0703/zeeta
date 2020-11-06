@call setenv.bat
@echo ----------------------------------------------------
@echo 自ZeetaのDBを初期化して他のZeetaのDBをコピーします。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
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
