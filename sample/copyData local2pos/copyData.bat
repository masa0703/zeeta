@call setenv.bat
@echo ----------------------------------------------------
@echo ��Zeeta��DB�����������đ���Zeeta��DB���R�s�[���܂��B��낵���ł����H
@echo �L�����Z������ꍇ�́ACTRL+C���^�C�v���Ă��������B
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
