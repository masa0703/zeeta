@call setenv.bat
@echo ----------------------------------------------------
@echo DB�����������܂��B��낵���ł����H
@echo �L�����Z������ꍇ�́ACTRL+C���^�C�v���Ă��������B
@echo ----------------------------------------------------
@pause
@del /Q db\*.*
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@java -cp %CLS% jp.tokyo.selj.util.DbSetup dbCopy.dicon
@pause
