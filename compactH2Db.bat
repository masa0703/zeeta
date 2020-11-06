@call setenv.bat
@echo ----------------------------------------------------
@echo H2データベースファイルを最適化します。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp %CLS% jp.tokyo.selj.util.CompactDb
@pause