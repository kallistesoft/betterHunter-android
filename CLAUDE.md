# Notes for Claude

## Design mockup

The BetterHunter design mockup (dark and light screens) is a claude.ai Design canvas:
https://claude.ai/artifact/NSq8SRajcbQ8UmAv5cJaer

To add or change screens, read the canvas with the Artifact tool (`action: "read"`, then its `project/canvas.json` and
the screens' `project/*.dc.html` files) and publish to that same URL; don't create a new canvas. Follow the palette,
fonts and patterns in `design/README.md`, and give every new screen a dark and a light version. After publishing,
copy the changed files into `design/mockup/` and update `design/README.md`.
