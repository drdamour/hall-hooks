//Test Fx Imports
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.time._
import org.junit.runner._

//3rd Party Imports
import play.api.libs.json.Json


//Our stuff imports
import models.Travis._


@RunWith(classOf[JUnitRunner])
class TravisSpec extends Specification with Mockito  {

  "Repository JSON" should {

    val exampleJson =
      """
        |{
        |  "id": 1,
        |  "name": "minimal",
        |  "owner_name": "svenfuchs",
        |  "url": "http://github.com/svenfuchs/minimal"
        |}
      """.stripMargin

    "be parsed to case classes" in {
      val json = Json.parse(exampleJson)

      val o = Json.fromJson[Repository](json).get

      val expected = Repository(1, "minimal", "svenfuchs", "http://github.com/svenfuchs/minimal")
      o must beEqualTo(expected)


    }
  }

  "Payload JSON for Push" should {

    val exampleJson =
      """
        |{
        |    "id": 17922525,
        |    "repository": {
        |        "id": 1825929,
        |        "name": "hall-hooks",
        |        "owner_name": "drdamour",
        |        "url": "https://github.com/drdamour/hall-hooks"
        |    },
        |    "number": "13",
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
        |    "status_message": "Passed",
        |    "result_message": "Passed",
        |    "started_at": "2014-01-30T17:52:32Z",
        |    "finished_at": "2014-01-30T18:09:02Z",
        |    "duration": 990,
        |    "build_url": "https://travis-ci.org/drdamour/hall-hooks/builds/17922525",
        |    "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
        |    "branch": "master",
        |    "message": "Fixed examples",
        |    "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
        |    "committed_at": "2014-01-30T17:49:37Z",
        |    "author_name": "drdamour",
        |    "author_email": "drdamour@gmail.com",
        |    "committer_name": "drdamour",
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
        |            "committer_name": "drdamour",
        |            "committer_email": "drdamour@gmail.com",
        |            "finished_at": "2014-01-30T18:09:02Z"
        |        }
        |    ],
        |    "type": "push"
        |}
      """.stripMargin

    "be parsed to case classes" in {
      val json = Json.parse(exampleJson)

      val validation = json.validate[BuildMessage];


      //Note you must specify the type you're trying to suck out, as it's ambiguous what format to use
      val o = Json.fromJson[BuildMessage](json).get

      val expected = BuildMessage(
        17922525,
        "13",
        0,
        Some("2014-01-30T17:52:32Z"),
        Some("2014-01-30T18:09:02Z"),
        "Passed",
        "89df55e2cee1393d08790a291a59ee1055ed3547",
        "master",
        "Fixed examples",
        "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
        "2014-01-30T17:49:37Z",
        "drdamour",
        "drdamour@gmail.com",
        "drdamour",
        "drdamour@gmail.com",
        "https://travis-ci.org/drdamour/hall-hooks/builds/17922525",
        Repository(1825929, "hall-hooks", "drdamour", "https://github.com/drdamour/hall-hooks"),
        None
      )

      o must beEqualTo(expected)
    }


  }


  "Payload JSON for Pull Request" should {

    val exampleJson =
      """
        |{
        |    "id": 17922525,
        |    "repository": {
        |        "id": 1825929,
        |        "name": "hall-hooks",
        |        "owner_name": "drdamour",
        |        "url": "https://github.com/drdamour/hall-hooks"
        |    },
        |    "number": "13",
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
        |    "status_message": "Passed",
        |    "result_message": "Passed",
        |    "started_at": "2014-01-30T17:52:32Z",
        |    "finished_at": "2014-01-30T18:09:02Z",
        |    "duration": 990,
        |    "build_url": "https://travis-ci.org/drdamour/hall-hooks/builds/17922525",
        |    "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
        |    "branch": "master",
        |    "message": "Fixed examples",
        |    "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
        |    "committed_at": "2014-01-30T17:49:37Z",
        |    "author_name": "drdamour",
        |    "author_email": "drdamour@gmail.com",
        |    "committer_name": "drdamour",
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
        |            "committer_name": "drdamour",
        |            "committer_email": "drdamour@gmail.com",
        |            "finished_at": "2014-01-30T18:09:02Z"
        |        }
        |    ],
        |   "type":"pull_request",
        |   "pull_request_number":20
        |}
      """.stripMargin

    "be parsed to case classes" in {
      val json = Json.parse(exampleJson)

      val validation = json.validate[BuildMessage];


      //Note you must specify the type you're trying to suck out, as it's ambiguous what format to use
      val o = Json.fromJson[BuildMessage](json).get

      val expected = BuildMessage(
        17922525,
        "13",
        0,
        Some("2014-01-30T17:52:32Z"),
        Some("2014-01-30T18:09:02Z"),
        "Passed",
        "89df55e2cee1393d08790a291a59ee1055ed3547",
        "master",
        "Fixed examples",
        "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
        "2014-01-30T17:49:37Z",
        "drdamour",
        "drdamour@gmail.com",
        "drdamour",
        "drdamour@gmail.com",
        "https://travis-ci.org/drdamour/hall-hooks/builds/17922525",
        Repository(1825929, "hall-hooks", "drdamour", "https://github.com/drdamour/hall-hooks"),
        Some(20)
      )

      o must beEqualTo(expected)
    }
  }

}

