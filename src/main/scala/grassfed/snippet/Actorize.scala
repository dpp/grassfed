package grassfed
package snippet

import net.liftweb.actor.LiftActor
import net.liftweb.http.RoundTripHandlerFunc
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{JsCrVar, Script}
import net.liftweb.json.JsonAST.JString

import net.liftweb.common._
import grassfed.lib._
import ClojureInterop._
import net.liftweb.util.{Helpers, Schedule}

object Actorize extends InSession {
  lazy val postMsg = findVar("grassfed.server.chat", "post-msg")
  lazy val fromServer = findVar("grassfed.server.bridge", "process")

  def cljBridge(raw: String, sender: RoundTripHandlerFunc): Unit = {
    fromServer.invoke(ClojureInterop.transitRead(raw), sender)
  }

  def render = {
    <tail>
      {
      val clientProxy =
        session.serverActorForClient("grassfed.client.core.receive",
          shutdownFunc = Full(actor => postMsg.invoke('remove -> actor)),
          dataFilter = transitWrite(_))

      postMsg.invoke('add -> clientProxy) // register with the chat server

      // Create a server-side Actor that will receive messages when
      // a function on the client is called
      val serverActor = new LiftActor {
        override protected def messageHandler =
        {
          case JString(str) => postMsg.invoke(ClojureInterop.transitRead(str))
        }
      }

      Script(JsRaw("var sendToServer = " + session.clientActorFor(serverActor).toJsCmd).cmd &
      JsCrVar("CljBridge", session.buildRoundtrip(List("send" -> cljBridge _))))
    }
    </tail>

  }

}

