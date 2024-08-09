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

const styles = theme => ({});

class IntinoFileBrowser extends AbstractIntinoFileBrowser {

	constructor(props) {
		super(props);
		this.notifier = new IntinoFileBrowserNotifier(this);
		this.requester = new IntinoFileBrowserRequester(this);
		this.state = {
		    itemAddress : "",
		    items: [],
		    focusedItem: null,
		    expandedItems: [],
		    selectedItems: [],
		};
	};

    render() {
        const id = this.props.id + "-tree";
        const theme = Theme.get();
        const style = theme.isDark() ? { backgroundColor:'#404348', color:'#e3e3e3'} : {};
        if (this.state.items.length <= 1) return (<div className="layout vertical flex center-center" style={{height:'100%',fontSize:'10pt'}}>{this.translate("No files")}</div>);
        return (
            <ControlledTreeEnvironment
              getItemTitle={item => item.data}
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
                    <Tree className="rct-dark" treeId={id} rootItem="root" treeLabel={this.translate("Files")} />
                </div>
            </ControlledTreeEnvironment>
        )
    };

    refresh = (info) => {
        this.setState({itemAddress: info.itemAddress, items: info.items});
    };

    select = (item) => {
        const items = this._itemsOf(this.state.items);
        const parents = items[item.name] != null ? items[item.name].parents : [];
        this.setState({selectedItems: [item.name], expandedItems: [...this.state.expandedItems, ...parents]});
    };

    _itemsOf = (items) => {
        const result = {};
        items.forEach(i => result[i.name] = this._itemOf(i));
        return result;
    };

    _itemOf = (item) => {
        return {
            index: item.name,
            data: item.name,
            id: item.id,
            isFolder: item.type == "Folder",
            parents: item.parents,
            children: item.children,
            canMove: true,
            canRename: true
        };
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
        history.push(this.state.itemAddress.replace(/:file/, items[0]), {});
        this.requester.open(items[0]);
        this.setState({selectedItems: items});
    };

}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoFileBrowser));
DisplayFactory.register("IntinoFileBrowser", withStyles(styles, { withTheme: true })(withSnackbar(IntinoFileBrowser)));