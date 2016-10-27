/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';

import * as path from 'path';

import { workspace, Disposable, ExtensionContext } from 'vscode';
import { LanguageClient, LanguageClientOptions, SettingMonitor, ServerOptions, TransportKind, Executable } from 'vscode-languageclient';

export function activate(context: ExtensionContext) {

	// The server is implemented in scala
	let serverBin = context.asAbsolutePath(path.join('server', 'bin', 'vscode-isabelle'));
	// The debug options for the server
	let debugOptions = { execArgv: ["--nolazy", "--debug=6004"] };

	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	let serverOptions: Executable = {
		command: serverBin
	}

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: ['isabelle'],
		synchronize: {
			// Synchronize the setting section 'languageServerExample' to the server
			// configurationSection: 'languageServerExample',
			// Notify the server about file changes to '.clientrc files contain in the workspace
			// fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
		},
		diagnosticCollectionName: 'isabelle'
	}

	let client = new LanguageClient('Isabelle', serverOptions, clientOptions)

	// Create the language client and start the client.
	let disposable = client.start();

	client.onNotification

	// Push the disposable to the context's subscriptions so that the
	// client can be deactivated on extension deactivation
	context.subscriptions.push(disposable);
}
