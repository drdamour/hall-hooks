package controllers

import play.api.test.{FakeRequest, WithApplication, PlaySpecification}
import org.specs2.mock.Mockito
import play.api.mvc.Security
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.i18n.{Lang, Messages}
import scala.util.Random


import org.joda.time.LocalDate
import play.api.templates.Html
import play.api.data.{FormError, Form}
import scala.Some
import play.mvc.Http.Status
import scala.concurrent.Await
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration.DurationInt
import hall.HallCommandHandlerSlice
import models.HallMessage
import play.api.libs.ws.Response
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


@RunWith(classOf[JUnitRunner])
class TravisControllerSpec extends PlaySpecification with Mockito with NoTimeConversions
{

  trait TestBatter extends HallCommandHandlerSlice {
    val hallCommandHandler = mock[HallCommandHandler]
  }


  "TravisController used for notification relay of branch" should {

    "send notification for valid json payload passed" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "Passed"
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"

      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeResponse = mock[Response]
      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = ""

      val expectedCodeSource = s"""branch <a target="_blank" href="$fakeRepoUrl/tree/$fakeBranch" >$fakeBranch</a>"""
      val expectedMessageText = s"""<a target="_blank" href="$fakeBuildUrl">Build $fakeBuildNumber</a> for $expectedCodeSource completed with status <b>${fakeStatusMessage.toUpperCase}</b> (<a target="_blank" href="$fakeCompareUrl">${fakeCommitId.substring(0, 6)}</a> by $fakeCommitterName - $fakeMessage)"""


      val expectedMessage = HallMessage(fakeRoomToken, fakeRepoName + " project build status", expectedMessageText, None)
      cake.hallCommandHandler.sendMessage(expectedMessage) returns Future(fakeResponse)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.SEE_OTHER)


    }


    "send notification for valid json payload pending" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "penDing" //case shouldn't matter
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"

      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeResponse = mock[Response]
      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = ""

      //val expectedCodeSource = s"""<a target="_blank" href="$fakeCompareUrl">Pull Request ${p.pull_request_number.get}</a>"""
      val expectedCodeSource = s"""branch <a target="_blank" href="$fakeRepoUrl/tree/$fakeBranch" >$fakeBranch</a>"""

      val expectedMessageText = s"""<a target="_blank" href="$fakeBuildUrl">Build $fakeBuildNumber</a> for $expectedCodeSource <b>started</b> (<a target="_blank" href="$fakeCompareUrl">${fakeCommitId.substring(0, 6)}</a> by $fakeCommitterName - $fakeMessage)"""


      val expectedMessage = HallMessage(fakeRoomToken, fakeRepoName + " project build status", expectedMessageText, None)
      cake.hallCommandHandler.sendMessage(expectedMessage) returns Future(fakeResponse)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.SEE_OTHER)


    }
  }

  "TravisController used for notification relay of pull-request" should {

    "send notification for valid json payload passed" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "Passed"
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"
      val fakePRNumber = "99"



      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "pull_request_number" : $fakePRNumber,
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeResponse = mock[Response]
      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = ""

      val expectedCodeSource = s"""<a target="_blank" href="$fakeCompareUrl">Pull Request $fakePRNumber</a>"""
      val expectedMessageText = s"""<a target="_blank" href="$fakeBuildUrl">Build $fakeBuildNumber</a> for $expectedCodeSource completed with status <b>${fakeStatusMessage.toUpperCase}</b> (<a target="_blank" href="$fakeCompareUrl">${fakeCommitId.substring(0, 6)}</a> by $fakeCommitterName - $fakeMessage)"""


      val expectedMessage = HallMessage(fakeRoomToken, fakeRepoName + " project build status", expectedMessageText, None)
      cake.hallCommandHandler.sendMessage(expectedMessage) returns Future(fakeResponse)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.SEE_OTHER)


    }


    "skip notification for valid json payload passed when passed is in ignored statuses" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "Passed"
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"
      val fakePRNumber = "99"



      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "pull_request_number" : $fakePRNumber,
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = "xxxPaSSedxx" //case should not matter


      there was no(cake.hallCommandHandler).sendMessage(any)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.OK)
      contentAsString(result) must beEqualTo("notification skipped")

    }

    "send notification for valid json payload for pull-request pending" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "penDing" //case shouldn't matter
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"
      val fakePRNumber = "99"

      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "pull_request_number" : $fakePRNumber,
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeResponse = mock[Response]
      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = ""

      val expectedCodeSource = s"""<a target="_blank" href="$fakeCompareUrl">Pull Request $fakePRNumber</a>"""
      val expectedMessageText = s"""<a target="_blank" href="$fakeBuildUrl">Build $fakeBuildNumber</a> for $expectedCodeSource <b>started</b> (<a target="_blank" href="$fakeCompareUrl">${fakeCommitId.substring(0, 6)}</a> by $fakeCommitterName - $fakeMessage)"""


      val expectedMessage = HallMessage(fakeRoomToken, fakeRepoName + " project build status", expectedMessageText, None)
      cake.hallCommandHandler.sendMessage(expectedMessage) returns Future(fakeResponse)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.SEE_OTHER)

    }


    "skip notification for valid json payload for pull-request pending when pending is in ignored statuses" in {

      //Important content of json
      val fakeRepoName ="some repo"
      val fakeStatusMessage = "penDing" //case shouldn't matter
      val fakeCompareUrl = "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1"
      val fakeRepoUrl = "https://github.com/drdamour/hall-hooks"
      val fakeBranch = "somebranch"
      val fakeMessage = "some commit message"
      val fakeCommitterName = "some name"
      val fakeCommitId = "89df55e2cee1393d08790a291a59ee1055ed3547"
      val fakeBuildNumber = "6543"
      val fakeBuildUrl = "https://travis-ci.org/drdamour/hall-hooks/builds/17922525"
      val fakePRNumber = "99"

      val validJson =
        s"""
          |{
          |    "id": 17922525,
          |    "repository": {
          |        "id": 1825929,
          |        "name": "$fakeRepoName",
          |        "owner_name": "drdamour",
          |        "url": "$fakeRepoUrl"
          |    },
          |    "number": "$fakeBuildNumber",
          |    "config": {
          |        "language": "scala",
          |        "scala": [
          |            "2.10.3"
          |        ],
          |        "deploy": {
          |            "provider": "heroku",
          |            "api_key": {
          |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
          |            },
          |            "app": "hall-hooks",
          |            "true": {
          |                "repo": "drdamour/hall-hooks"
          |            }
          |        },
          |        "notifications": {
          |            "webhooks": [
          |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |            ]
          |        },
          |        ".result": "configured"
          |    },
          |    "status": 0,
          |    "result": 0,
          |    "status_message": "$fakeStatusMessage",
          |    "result_message": "Passed",
          |    "started_at": "2014-01-30T17:52:32Z",
          |    "finished_at": "2014-01-30T18:09:02Z",
          |    "duration": 990,
          |    "build_url": "$fakeBuildUrl",
          |    "commit": "$fakeCommitId",
          |    "branch": "$fakeBranch",
          |    "message": "$fakeMessage",
          |    "compare_url": "$fakeCompareUrl",
          |    "committed_at": "2014-01-30T17:49:37Z",
          |    "author_name": "drdamour",
          |    "author_email": "drdamour@gmail.com",
          |    "committer_name": "$fakeCommitterName",
          |    "committer_email": "drdamour@gmail.com",
          |    "matrix": [
          |        {
          |            "id": 17922526,
          |            "repository_id": 1825929,
          |            "parent_id": 17922525,
          |            "number": "13.1",
          |            "state": "finished",
          |            "config": {
          |                "language": "scala",
          |                "scala": "2.10.3",
          |                "notifications": {
          |                    "webhooks": [
          |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
          |                    ]
          |                },
          |                ".result": "configured",
          |                "addons": {}
          |            },
          |            "status": null,
          |            "result": null,
          |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
          |            "branch": "master",
          |            "message": "Fixed examples",
          |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
          |            "committed_at": "2014-01-30T17:49:37Z",
          |            "author_name": "drdamour",
          |            "author_email": "drdamour@gmail.com",
          |            "committer_name": "$fakeCommitterName",
          |            "committer_email": "drdamour@gmail.com",
          |            "finished_at": "2014-01-30T18:09:02Z"
          |        }
          |    ],
          |    "pull_request_number" : $fakePRNumber,
          |    "type": "push"
          |}
        """.stripMargin

      val cake = new TravisController with TestBatter

      val fakeRoomToken = "12345"
      val fakeIgnoredStatuses = "234958pENding343"

      there was no(cake.hallCommandHandler).sendMessage(any)

      val request = FakeRequest(POST, s"/travis-ci/buildnotification/$fakeRoomToken")
        .withFormUrlEncodedBody("payload"->validJson)

      val result = cake.sendBuildStatusToHall(fakeRoomToken, fakeIgnoredStatuses)(request)

      status(result) must beEqualTo(Status.OK)
      contentAsString(result) must beEqualTo("notification skipped")

    }


  }



}
