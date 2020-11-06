@call setenv.bat
@echo ----------------------------------------------------
@echo DBを初期化します。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@pause