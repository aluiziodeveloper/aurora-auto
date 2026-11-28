#!/usr/bin/env bash
# Takes the current branch to main the same way every time:
# commit, push, pull request, green checks, squash merge and, optionally, a tag.
#
#   scripts/pr.sh "<conventional commit title>" [tag]
#   scripts/pr.sh "ci: protect main with a ruleset and validate PR titles" aula-01-06
#
# Runs on the host: it needs git and gh, not the dev container.
set -euo pipefail

title="${1:-}"
tag="${2:-}"

if [[ -z "$title" ]]; then
  echo "usage: $0 \"<conventional commit title>\" [tag]" >&2
  exit 2
fi

cd "$(git rev-parse --show-toplevel)"
scripts/check-pr-title.sh "$title"

branch="$(git branch --show-current)"
main="$(gh repo view --json defaultBranchRef --jq .defaultBranchRef.name)"
if [[ "$branch" == "$main" || -z "$branch" ]]; then
  echo "run it from a feature branch, not from '${branch:-detached HEAD}'" >&2
  exit 1
fi
if [[ -n "$tag" ]] && git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
  echo "tag $tag already exists" >&2
  exit 1
fi

# 1. Commit whatever is still pending, with the title as the message.
if [[ -n "$(git status --porcelain)" ]]; then
  git add -A
  git commit -m "$title"
fi

# 2. Push and open the pull request (or reuse the open one).
git push -u origin "$branch"
if ! number="$(gh pr view "$branch" --json number,state --jq 'select(.state == "OPEN") | .number' 2>/dev/null)" || [[ -z "$number" ]]; then
  gh pr create --base "$main" --head "$branch" --title "$title" --body ""
  number="$(gh pr view "$branch" --json number --jq .number)"
else
  gh pr edit "$number" --title "$title"
fi
echo "pull request #$number"

# 3. Wait for the checks to show up, then for all of them to finish.
for _ in $(seq 30); do
  if gh pr checks "$number" --json name --jq 'length' 2>/dev/null | grep -qv '^0$'; then
    break
  fi
  sleep 2
done
gh pr checks "$number" --watch --fail-fast

# 4. Squash merge. Subject and body are explicit, so the commit on main carries
#    exactly the title and nothing else (no trailers). No --delete-branch: the
#    repository already deletes the remote branch on merge (delete_branch_on_merge,
#    lesson 01-06), and asking gh to delete it too races with GitHub (HTTP 404).
gh pr merge "$number" --squash --subject "$title (#$number)" --body ""

# 5. Bring main up to date, drop the local branch and tag the merge commit.
#    -D, not -d: after a squash merge, git does not see the branch as merged.
git switch "$main"
git pull --ff-only origin "$main"
git branch -D "$branch"
if [[ -n "$tag" ]]; then
  subject="$(git log -1 --format=%s)"
  if [[ "$subject" != "$title (#$number)" ]]; then
    echo "HEAD is '$subject', not the merge of #$number; tag not created" >&2
    exit 1
  fi
  git tag "$tag"
  git push origin "$tag"
  echo "tag $tag on $(git rev-parse --short HEAD)"
fi
