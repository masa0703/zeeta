@echo ----------------------------------------------------
@echo DBを初期化します。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp ../lib/selj.jar jp.tokyo.selj.util.DbSetup
@pause