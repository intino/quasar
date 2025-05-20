import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoFileBrowser from "../../gen/displays/AbstractIntinoFileBrowser";
import IntinoFileBrowserNotifier from "../../gen/displays/notifiers/IntinoFileBrowserNotifier";
import IntinoFileBrowserRequester from "../../gen/displays/requesters/IntinoFileBrowserRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import { ControlledTreeEnvironment, Tree } from 'react-complex-tree';
import 'react-complex-tree/lib/style-modern.css';
import Theme from "app-elements/gen/Theme";
import history from "alexandria-ui-elements/src/util/History";
import { Popover } from '@material-ui/core';
import classnames from 'classnames';
import 'alexandria-ui-elements/res/styles/layout.css';

const styles = theme => ({
    contextMenu : {
        border: "1px solid #ccc",
        borderRadius: "5px",
        padding: "10px",
        width: "300px",
        height: "145px",
        overflow: "auto",
        backgroundColor: theme.isDark() ? "black" : "white",
    },
    operation: {
        cursor: 'pointer',
        width: "100%",
        padding: "5px 20px 5px 20px",
        fontSize: "10pt",
        borderRadius: "5px",
        '&:hover' : {
            background: "#135fb8",
            color: "white",
        },
    },
    operationDisabled : {
        color: 'grey',
        cursor: 'default',
        '&:hover' : {
            background: "none !important",
            color: "grey",
        },
    },
    operationShortcut: {
        fontSize: "8pt"
    }
});

class IntinoFileBrowser extends AbstractIntinoFileBrowser {

	constructor(props) {
		super(props);
		this.notifier = new IntinoFileBrowserNotifier(this);
		this.requester = new IntinoFileBrowserRequester(this);
		this.state = {
		    itemAddress : "",
		    rootItem: "root",
		    items: [],
		    operations: [],
		    focusedItem: null,
		    expandedItems: [],
		    selectedItems: [],
		    contextMenuTrigger: null,
		    contextMenuPosition: { x: 0, y: 0 },
		    hideExtension: false,
		    historyEnabled: true,
		};
	};

	componentDidMount() {
        this.registerShortcutListener();
    };

    render() {
        const id = this.props.id + "-tree";
        const theme = Theme.get();
        const style = theme.isDark() ? { backgroundColor:'#1f1f1f', color:'#e3e3e3'} : {};
        const root = this.state.rootItem != null ? this.state.rootItem : "root";
        const hideExtension = this.state.hideExtension;
        return (
            <div id={this.props.id + "-container"} onContextMenu={this.handleOpenContextMenu.bind(this)} style={{position:'relative'}}>
                {this.renderContextMenu()}
                <ControlledTreeEnvironment
                  getItemTitle={item => item.data}
                  renderItemTitle={(props) => !hideExtension || props.item.isFolder ? props.title : props.title.substr(0, props.title.lastIndexOf(".")) }
                  viewState={{ [id]: { focusedItem: this.state.focusedItem, expandedItems: this.state.expandedItems, selectedItems: this.state.selectedItems } }}
                  canDragAndDrop={true}
                  canDropOnFolder={true}
                  canReorderItems={true}
                  canDropAt={e => { return true }}
                  onSelectItems={this.handleSelectItems.bind(this)}
                  onFocusItem={this.handleFocusItem.bind(this)}
                  onExpandItem={this.handleExpandItem.bind(this)}
                  onCollapseItem={this.handleCollapseItem.bind(this)}
                  onRenameItem={this.handleRenameItem.bind(this)}
                  onDrop={this.handleDrop.bind(this)}
                  items={this._itemsOf(this.state.items)}>
                    <div className={theme.isDark() ? "rct-dark" : "rct"} style={style}>
                        <Tree className="rct-dark" treeId={id} rootItem={root} treeLabel={this.translate("Files")} />
                    </div>
                </ControlledTreeEnvironment>
            </div>
        )
    };

    renderContextMenu = () => {
        const { classes } = this.props;
        const trigger = this.state.contextMenuTrigger;
        const position = this.state.contextMenuPosition;
		return (
			<Popover open={trigger != null}
			        onClose={this.handleCloseContextMenu.bind(this)}
			        anchorEl={this.props.id + "-container"}
			        style={{marginLeft: (position.x-10) + "px", marginTop: (position.y-10) + "px" }}
					disableRestoreFocus>
                <div className={classes.contextMenu}>
                    {this.renderOperations()}
                </div>
            </Popover>
		);
    };

    renderOperations = () => {
        const result = [];
        const operations = this.state.operations;
        for (var i=0; i<operations.length; i++) result.push(this.renderOperation(operations[i], i));
        return result;
    };

    renderOperation = (operation, index) => {
        const { classes } = this.props;
        return (
            <div className={classnames(classes.operation, "layout horizontal center", operation.enabled ? "" : classes.operationDisabled)}>
                <a disabled={!operation.enabled} className="layout horizontal flex" onClick={this.handleExecuteOperation.bind(this, operation)}>{operation.name}</a>
                {(operation.shortcut != null && operation.shortcut !== "") && <div className={classnames(classes.operationShortcut, "layout horizontal end-justified")}>{this.shortcutLabel(operation.shortcut)}</div>}
            </div>
        );
    };

    refresh = (info) => {
        this.setState({
            itemAddress: info.itemAddress,
            items: info.items,
            rootItem: info.rootItem,
            operations: info.operations,
            hideExtension: info.hideExtension,
            historyEnabled: info.historyEnabled
        });
    };

    shortcutLabel = (shortcut) => {
        var result = shortcut.key;
        if (shortcut.ctrlKey) result = "Ctrl + " + result;
        if (shortcut.shiftKey) result = "Shift + " + result;
        if (shortcut.altKey) result = "Alt + " + result;
        return result;
    };

    openContextMenu = (enableList) => {
        const target = document.getElementById(this.props.id + "-container");
        const info = target.getBoundingClientRect();
        this.setState({contextMenuTrigger: this.props.id + "-tree", contextMenuPosition: { x: info.right - 100, y: info.top }});
    };

    select = (item) => {
        const items = this._itemsOf(this.state.items);
        const parents = item != null ? (items[item.uri] != null ? items[item.uri].parents : []) : [];
        this.setState({selectedItems: item != null ? [item.uri] : [], expandedItems: [...this.state.expandedItems, ...parents]});
    };

    _itemsOf = (items) => {
        const result = {};
        items.forEach(i => result[i.uri] = this._itemOf(i));
        return result;
    };

    _itemOf = (item) => {
        return {
            index: item.uri,
            data: item.name,
            id: item.id,
            isFolder: item.type == "Folder",
            parents: item.parents,
            children: item.children,
            canMove: true,
            canRename: true
        };
    };

    handleOpenContextMenu = (e) => {
        e.preventDefault();
        window.event.returnValue = false;
        this.setState({contextMenuTrigger:e.target, contextMenuPosition: { x: e.clientX, y: e.clientY }});
        return false;
    };

    handleCloseContextMenu = () => {
        this.setState({contextMenuTrigger:null});
    };

    handleExecuteOperation = (operation) => {
        if (!operation.enabled) return;
        this.requester.executeOperation({operation: operation.name, target: null});
        this.setState({contextMenuTrigger:null});
    };

    handleFocusItem = (item) => {
        this.setState({focusedItem: item.index});
    };

    handleExpandItem = (item) => {
        this.setState({expandedItems: [...this.state.expandedItems, item.index]});
    };

    handleCollapseItem = (item) => {
        this.setState({expandedItems: this.state.expandedItems.filter(expandedItemIndex => expandedItemIndex !== item.index)});
    };

    handleRenameItem = (item, name, treeId) => {
        this.requester.rename({id: item.id, newName: name});
    };

    handleDrop = (items, target) => {
        this.requester.move({file: items[0].id, directory: target.targetItem != null ? target.targetItem : target.parentItem});
    };

    handleSelectItems = (items, treeId) => {
        if (items.length <= 0) return;
        if (this.state.historyEnabled) history.push(this.state.itemAddress.replace(/:file/, items[0]), {});
        this.requester.open(items[0]);
        this.setState({selectedItems: items});
    };

	registerShortcutListener = () => {
        const widget = this;
        widget.pressed = [];
        document.addEventListener("keydown", (event) => {
            widget.pressed[event.key.toLowerCase()] = true;
            const operation = this.findOperationWithShortcut(event.key.toLowerCase());
            if (operation != null) {
                delete widget.pressed[event.key.toLowerCase()];
                widget.handleExecuteOperation(operation);
                return false;
            }
        });
        document.addEventListener("keyup", (event) => {
            delete widget.pressed[event.key.toLowerCase()];
        });
	};

    findOperationWithShortcut = (key) => {
        const operations = this.state.operations;
        const operationsMatched = operations.filter(o => {
            const shortcut = o.shortcut;
            if (shortcut == null) return;
            return ((!shortcut.ctrlKey || ((this.pressed["control"] || this.pressed["meta"]) && shortcut.ctrlKey)) &&
                    (!shortcut.shiftKey || (this.pressed["shift"] === true && shortcut.shiftKey)) &&
                    (!shortcut.altKey || (this.pressed["alt"] && shortcut.altKey)) &&
                    (key === shortcut.key.toLowerCase())
            );
        });
        if (operationsMatched.length == 0) return null;
        if (operationsMatched.length == 1) return operationsMatched[0];
        var selected = operationsMatched[0];
        for (var i=1; i<operationsMatched.length; i++) {
            const countSelectedSpecialKeys = this.countSpecialKeys(selected);
            const countCurrentSpecialKeys = this.countSpecialKeys(operationsMatched[i]);
            if (countCurrentSpecialKeys > countSelectedSpecialKeys) selected = operationsMatched[i];
        }
        return selected;
    };

    countSpecialKeys = (operation) => {
        let count = 0;
        if (operation.shortcut.ctrlKey) count++;
        if (operation.shortcut.altKey) count++;
        if (operation.shortcut.shiftKey) count++;
        return count;
    };

}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoFileBrowser));
DisplayFactory.register("IntinoFileBrowser", withStyles(styles, { withTheme: true })(withSnackbar(IntinoFileBrowser)));