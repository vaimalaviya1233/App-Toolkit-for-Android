# Using GitHub Wikis

This page summarizes how to manage a wiki for your repository.

## Overview

GitHub wikis allow you to host long-form documentation alongside your project. Each wiki is a Git repository so you can edit pages on GitHub or locally using a typical Git workflow. Public repository wikis are visible to everyone, while private repository wikis are limited to collaborators.

## Adding or Editing Pages

1. Navigate to the **Wiki** tab of your repository.
2. Click **New Page** to create a page or **Edit** on an existing page.
3. Write content in Markdown or any other supported format, then commit the change with a descriptive message.

You can also clone the wiki repository with `git clone https://github.com/USER/REPO.wiki.git` and push changes from your computer. Only commits on the default branch appear on the wiki.

## Custom Sidebar or Footer

Add a page named `_Sidebar.<extension>` or `_Footer.<extension>` to define a sidebar or footer. Edit them like any other page, and GitHub will render them on every wiki page.

## Viewing History and Reverting Changes

Each edit to a wiki page creates a commit. Click the revision link at the top of a page to view its history, compare revisions, or revert changes if you have write access.

## Access Permissions and Disabling Wikis

By default, only collaborators can edit a repository's wiki. Repository owners can allow public editing or disable the wiki entirely from the **Settings** tab under **Features**.

## Notes

- Search engines only index wikis with 500+ stars that disallow public editing.
- Wikis have a soft limit of 5,000 files. Use GitHub Pages if you require a larger or indexed site.

