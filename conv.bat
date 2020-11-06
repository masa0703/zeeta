@call setenv.bat
@echo ----------------------------------------------------
@echo DBを旧形式から新形式に変換します。よろしいですか？
@echo キャンセルする場合は、CTRL+Cをタイプしてください。
@echo ----------------------------------------------------
@pause
@java -cp %CLS% sel.util.TableConvertor
@pause