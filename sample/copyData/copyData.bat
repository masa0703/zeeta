@call setenv.bat
@echo ----------------------------------------------------
@echo ��Zeeta��DB�����������đ���Zeeta��DB���R�s�[���܂��B��낵���ł����H
@echo �L�����Z������ꍇ�́ACTRL+C���^�C�v���Ă��������B
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
