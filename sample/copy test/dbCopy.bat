@call setenv.bat
@echo ----------------------------------------------------
@echo DBを初期化します。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@del /Q db\*.*
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@java -cp %CLS% jp.tokyo.selj.util.DbSetup dbCopy.dicon
@pause
