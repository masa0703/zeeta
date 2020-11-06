@call setenv.bat
@echo ----------------------------------------------------
@echo MSAccessデータを取り込みます。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp %CLS% jp.tokyo.selj.util.Migrate
@pause