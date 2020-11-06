@call setenv.bat
@echo ----------------------------------------------------
@echo 自ZeetaのDBを初期化して他のZeetaのDBをコピーします。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@java -cp %CLS% ^
-DsrcDb.driver=org.h2.Driver ^
-DsrcDb.url=jdbc:h2:file:../../db/sel ^
-DsrcDb.user=sa ^
-DsrcDb.password= ^
jp.tokyo.selj.util.CopyData 
@pause
