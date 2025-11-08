.PHONY: push pull update production solved_merge

SHELL=/bin/bash

INITIAL_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)

push:
	@echo -e "\033[1;34m📤 Пуш в ветку: $(INITIAL_BRANCH)\033[0m"

	@echo -n "⏳ Пуш через: "
	@for i in 10 9 8 7 6 5 4 3 2 1; do \
		echo -n "$$i... "; \
		sleep 1; \
	done; \
	echo ""
	git add .
	@git diff --cached --quiet || git commit -m "$(INITIAL_BRANCH) $(shell date '+%Y-%m-%d %H:%M:%S')"
	git push origin $(INITIAL_BRANCH)


pull:
	git pull origin $(INITIAL_BRANCH)