package net.flatmap.vscode

import akka.stream.Materializer
import io.circe.Json
import net.flatmap.vscode.languageserver._

import scala.concurrent._
import info.hupel.isabelle.setup.Setup
import info.hupel.isabelle.api.Version
import isabelle.{Future => _, Position => _, _}

class IsabelleServer(client: LanguageClient)(implicit ec: ExecutionContext, mat: Materializer) extends
  LanguageServer
  with ServerCapabilities.CompletionProvider
  with TextDocuments
  with Configuration[Config] {

  var maxNumberOfProblems = 100

  val session = Promise[Session]

  def initialize(processId: Option[Int],
                 rootPath: Option[String],
                 initializationOptions: Option[Json],
                 capabilities: ClientCapabilities): Future[InitializeResult] = {

    val setup = Setup.defaultSetup(Version("2016")).toOption.get

    textDocuments.runForeach { case (uri,versions) =>
      session.future.foreach { session =>
        session.update(Document.Blobs(Map.empty),Nil)
      }
    }

    for {
      setup <- setup
    } yield {
      client.window.logMessage(MessageType.Info, "initializing isabelle 2016...")
      Isabelle_System.init(setup.home.toString)
      val ops = isabelle.Options.init()
      val progress = new Progress {
        override def echo(txt: String) =
          client.window.logMessage(MessageType.Info,txt)
        override def theory(session: String, theory: String): Unit =
          client.window.logMessage(MessageType.Info,session + ": theory " + theory)
      }
      val sessionDirs = rootPath.map(Path.explode).toList
      Build.build(ops,progress = progress,build_heap = true,no_build = false,dirs = sessionDirs,sessions = List("HOL"))
      val content = isabelle.Build.session_content(ops,false,rootPath.map(Path.explode).toList,"HOL")
      val ses = new Session(new Resources(content.loaded_theories,content.known_theories,content.syntax))
      ses.start("vscode",Nil)
      session.trySuccess(ses)
      ses.syslog_messages += Session.Consumer("vscode") { msg =>
        client.window.logMessage(MessageType.Info, "[isabelle] " + XML.content(msg.message))
      }
      ses.commands_changed += Session.Consumer("vscode") { msg =>

      }

      InitializeResult(this.capabilities)
    }
  }

  def shutdown(): Future[Unit] =
    Future.successful()

  def exit(): Unit =
    sys.exit()

  def validateTextDocument(textDocument: TextDocumentItem): Unit = {
    val diags = textDocument.text.lines.toSeq.zipWithIndex.flatMap { case (line,n) =>
      val index = line.indexOf("typescript")
      if (index >= 0) Seq[Diagnostic](
        Diagnostic(
          range = Range(n,index,n,index + 10),
          severity = Some(DiagnosticSeverity.Warning),
          code = None,
          message = s"${line.substring(index,10)} should be spelled TypeScript",
          source = Some("ex")
        )
      )
      else Seq.empty[Diagnostic]
    }.take(maxNumberOfProblems)
    client.textDocument.publishDiagnostics(textDocument.uri, diags)
  }

  def didChangeWatchedFiles(changes: Seq[FileEvent]): Unit = {
    // Monitored files have changed in VSCode
    client.window.logMessage(MessageType.Log, "We received a file change event")
  }

  override def completionOptions: CompletionOptions =
    CompletionOptions(resolveProvider = Some(true))

  def completion(textDocument: TextDocumentIdentifier,
                 position: Position): Future[CompletionList] =
    Future.successful(CompletionList(Seq.empty))

  def resolveCompletionItem(item: CompletionItem): Future[CompletionItem] =
    Future.successful(item)
}