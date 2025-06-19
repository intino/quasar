import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractEditorHelpDisplay from "../../gen/displays/AbstractEditorHelpDisplay";
import EditorHelpDisplayNotifier from "../../gen/displays/notifiers/EditorHelpDisplayNotifier";
import EditorHelpDisplayRequester from "../../gen/displays/requesters/EditorHelpDisplayRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';

const styles = theme => ({});

const EditorHelpDisplaySections = {
  "Editing": [
    "Cut",
    "Copy",
    "Paste",
    "Undo",
    "Redo",
    "Delete line",
    "Insert line below",
    "Insert line above",
    "Format document",
    "Toggle line comment",
    "Toggle block comment"
  ],
  "Navigation": [
    "Go to definition",
    "Peek definition",
    "Go to symbol",
    "Go to line",
    "Go to next error/warning",
    "Go to previous error/warning",
    "Go to next difference",
    "Go to previous difference"
  ],
  "Search": [
    "Find",
    "Replace",
    "Find in files"
  ],
  "Multi-cursor & Selection": [
    "Select word",
    "Select next occurrence",
    "Select all occurrences",
    "Add cursor above",
    "Add cursor below",
    "Column (box) selection"
  ],
  "Code Structure": [
    "Fold code",
    "Unfold code",
    "Increase indent",
    "Decrease indent"
  ],
  "Command Palette & Language": [
    "Show all commands",
    "Change file language",
    "Trigger suggestions"
  ],
  "File & Save": [
    "Save (if supported)"
  ],
  "Accessibility": [
    "Toggle tab key moves focus",
    "Toggle high contrast theme"
  ],
  "Line Operations": [
    "Move line up",
    "Move line down",
    "Copy line up",
    "Copy line down"
  ]
};
const EditorHelpDisplayShortcuts = {
    "win": [
      { "action": "Autocomplete", "shortcut": "Ctrl + Space" },
      { "action": "Go to definition", "shortcut": "F12" },
      { "action": "Go to definition (new tab)", "shortcut": "Ctrl + Click" },
      { "action": "Peek definition", "shortcut": "Alt + F12" },
      { "action": "Go to symbol", "shortcut": "Ctrl + Shift + O" },
      { "action": "Go to line", "shortcut": "Ctrl + G" },
      { "action": "Find", "shortcut": "Ctrl + F" },
      { "action": "Replace", "shortcut": "Ctrl + H" },
      { "action": "Find in files", "shortcut": "Ctrl + Shift + F" },
      { "action": "Toggle line comment", "shortcut": "Ctrl + /" },
      { "action": "Fold code", "shortcut": "Ctrl + Shift + [" },
      { "action": "Unfold code", "shortcut": "Ctrl + Shift + ]" },
      { "action": "Increase indent", "shortcut": "Tab or Ctrl + ]" },
      { "action": "Decrease indent", "shortcut": "Shift + Tab or Ctrl + [" },
      { "action": "Toggle block comment", "shortcut": "Ctrl + Shift + A" },
      { "action": "Cut", "shortcut": "Ctrl + X" },
      { "action": "Copy", "shortcut": "Ctrl + C" },
      { "action": "Paste", "shortcut": "Ctrl + V" },
      { "action": "Select all", "shortcut": "Ctrl + A" },
      { "action": "Select word", "shortcut": "Ctrl + D" },
      { "action": "Select next occurrence", "shortcut": "Ctrl + D" },
      { "action": "Add cursor above", "shortcut": "Ctrl + Alt + Up" },
      { "action": "Add cursor below", "shortcut": "Ctrl + Alt + Down" },
      { "action": "Select all occurrences", "shortcut": "Ctrl + Shift + L" },
      { "action": "Column (box) selection", "shortcut": "Shift + Alt + Arrow keys" },
      { "action": "Move line up", "shortcut": "Alt + Up" },
      { "action": "Move line down", "shortcut": "Alt + Down" },
      { "action": "Copy line up", "shortcut": "Shift + Alt + Up" },
      { "action": "Copy line down", "shortcut": "Shift + Alt + Down" },
      { "action": "Delete line", "shortcut": "Ctrl + Shift + K" },
      { "action": "Insert line below", "shortcut": "Ctrl + Enter" },
      { "action": "Insert line above", "shortcut": "Ctrl + Shift + Enter" },
      { "action": "Undo", "shortcut": "Ctrl + Z" },
      { "action": "Redo", "shortcut": "Ctrl + Y / Ctrl + Shift + Z" },
      { "action": "Show all commands", "shortcut": "F1 / Ctrl + Shift + P" },
      { "action": "Change file language", "shortcut": "Ctrl + K M" },
      { "action": "Format document", "shortcut": "Shift + Alt + F" },
      { "action": "Trigger suggestions", "shortcut": "Ctrl + Space" },
      { "action": "Save (if supported)", "shortcut": "Ctrl + S" },
      { "action": "Go to next error/warning", "shortcut": "F8" },
      { "action": "Go to previous error/warning", "shortcut": "Shift + F8" },
      { "action": "Go to next difference", "shortcut": "F7" },
      { "action": "Go to previous difference", "shortcut": "Shift + F7" },
      { "action": "Toggle tab key moves focus", "shortcut": "Ctrl + M" },
      { "action": "Toggle high contrast theme", "shortcut": "Command Palette: Toggle High Contrast Theme" }
    ],
    "mac": [
      { "action": "Autocomplete", "shortcut": "Cmd + Space" },
      { "action": "Go to definition", "shortcut": "F12" },
      { "action": "Go to definition (new tab)", "shortcut": "Cmd + Click" },
      { "action": "Peek definition", "shortcut": "Option + F12" },
      { "action": "Go to symbol", "shortcut": "Cmd + Shift + O" },
      { "action": "Go to line", "shortcut": "Cmd + G" },
      { "action": "Find", "shortcut": "Cmd + F" },
      { "action": "Replace", "shortcut": "Cmd + H" },
      { "action": "Find in files", "shortcut": "Cmd + Shift + F" },
      { "action": "Toggle line comment", "shortcut": "Cmd + /" },
      { "action": "Fold code", "shortcut": "Cmd + Option + [" },
      { "action": "Unfold code", "shortcut": "Cmd + Option + ]" },
      { "action": "Increase indent", "shortcut": "Tab or Cmd + ]" },
      { "action": "Decrease indent", "shortcut": "Shift + Tab or Cmd + [" },
      { "action": "Toggle block comment", "shortcut": "Cmd + Shift + A" },
      { "action": "Cut", "shortcut": "Cmd + X" },
      { "action": "Copy", "shortcut": "Cmd + C" },
      { "action": "Paste", "shortcut": "Cmd + V" },
      { "action": "Select all", "shortcut": "Cmd + A" },
      { "action": "Select word", "shortcut": "Cmd + D" },
      { "action": "Select next occurrence", "shortcut": "Cmd + D" },
      { "action": "Add cursor above", "shortcut": "Cmd + Option + Up" },
      { "action": "Add cursor below", "shortcut": "Cmd + Option + Down" },
      { "action": "Select all occurrences", "shortcut": "Cmd + Shift + L" },
      { "action": "Column (box) selection", "shortcut": "Shift + Option + Arrow keys" },
      { "action": "Move line up", "shortcut": "Option + Up" },
      { "action": "Move line down", "shortcut": "Option + Down" },
      { "action": "Copy line up", "shortcut": "Shift + Option + Up" },
      { "action": "Copy line down", "shortcut": "Shift + Option + Down" },
      { "action": "Delete line", "shortcut": "Cmd + Shift + K" },
      { "action": "Insert line below", "shortcut": "Cmd + Enter" },
      { "action": "Insert line above", "shortcut": "Cmd + Shift + Enter" },
      { "action": "Undo", "shortcut": "Cmd + Z" },
      { "action": "Redo", "shortcut": "Cmd + Shift + Z" },
      { "action": "Show all commands", "shortcut": "F1 / Cmd + Shift + P" },
      { "action": "Change file language", "shortcut": "Cmd + K M" },
      { "action": "Format document", "shortcut": "Shift + Option + F" },
      { "action": "Trigger suggestions", "shortcut": "Cmd + Space" },
      { "action": "Save (if supported)", "shortcut": "Cmd + S" },
      { "action": "Go to next error/warning", "shortcut": "F8" },
      { "action": "Go to previous error/warning", "shortcut": "Shift + F8" },
      { "action": "Go to next difference", "shortcut": "F7" },
      { "action": "Go to previous difference", "shortcut": "Shift + F7" },
      { "action": "Toggle tab key moves focus", "shortcut": "Cmd + M" },
      { "action": "Toggle high contrast theme", "shortcut": "Command Palette: Toggle High Contrast Theme" }
    ]
};

class EditorHelpDisplay extends AbstractEditorHelpDisplay {

	constructor(props) {
		super(props);
		this.notifier = new EditorHelpDisplayNotifier(this);
		this.requester = new EditorHelpDisplayRequester(this);
		this.state = {
		    ...this.state,
		    content: ""
		}
	};

	render() {
	    const sections = EditorHelpDisplaySections;
	    const data = this.shortcuts();

	    return (
            <div style={{ fontFamily: "sans-serif", fontSize: 14 }}>
              {Object.entries(sections).map(([section, actions]) => {
                const filtered = data.filter(entry => actions.includes(entry.action));
                if (filtered.length === 0) return null;

                return (
                  <div key={section}>
                    <h3 style={{ fontFamily: "monospace", fontSize: "1.1em", borderBottom: "1px solid #ddd", paddingBottom: 4, marginTop: 24 }}>
                      {section}
                    </h3>
                    <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: 24 }}>
                      <thead>
                        <tr>
                          <th style={{ border: "1px solid #ccc", padding: 8, textAlign: "left", width: "50%" }}>Action</th>
                          <th style={{ border: "1px solid #ccc", padding: 8, textAlign: "left", width: "50%" }}>Shortcut</th>
                        </tr>
                      </thead>
                      <tbody>
                        {filtered.map((entry, idx) => (
                          <tr key={idx}>
                            <td style={{ border: "1px solid #ccc", padding: 8 }}>{entry.action}</td>
                            <td style={{ border: "1px solid #ccc", padding: 8, fontFamily: "monospace" }}>
                              <code>{entry.shortcut}</code>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                );
              })}
            </div>
        );
	};

	shortcuts = () => {
	    return EditorHelpDisplayShortcuts[this.detectOS()];
	}

	detectOS = () => {
	    return /mac/i.test(navigator.platform) ? "mac" : "win";
	};

	refresh = () => {
	    this.setState({content:"render"});
	};
}

export default withStyles(styles, { withTheme: true })(withSnackbar(EditorHelpDisplay));
DisplayFactory.register("EditorHelpDisplay", withStyles(styles, { withTheme: true })(withSnackbar(EditorHelpDisplay)));