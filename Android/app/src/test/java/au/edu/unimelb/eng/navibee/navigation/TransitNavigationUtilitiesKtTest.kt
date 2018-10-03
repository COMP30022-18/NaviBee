package au.edu.unimelb.eng.navibee.navigation

import android.graphics.Color.parseColor
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(android.graphics.Color::class)
@PowerMockIgnore("javax.net.ssl.*")
class TransitNavigationUtilitiesKtTest {

    @Before
    fun setUp() {

        PowerMockito.mockStatic(android.graphics.Color::class.java)

        `when`(parseColor(anyString())).thenReturn(anyInt())

    }

    @Test
    fun testParseResponseJson() {

        val json = """
{
  "Res": {
    "serviceUrl": "http://internal-ptkernel-prd-v289-58942325.ap-southeast-1.elb.amazonaws.com/goroute/AUSTRALIA_V753_20180927",
    "Connections": {
      "valid_until": "2018-10-05",
      "context": "GVX9bdsHbrjmXnckqAVuuP__AACszbFbEv0C_9AHZACg2bFbEv0",
      "Connection": [
        {
          "id": "R0017c3-C0",
          "duration": "PT51M",
          "transfers": 2,
          "Dep": {
            "time": "2018-10-01T07:33:00",
            "Stn": {
              "y": -37.799157,
              "x": 144.994451,
              "name": "Victoria Park Railway Station (Abbotsford)",
              "id": "721280150"
            }
          },
          "Arr": {
            "time": "2018-10-01T08:24:00",
            "Addr": {
              "y": -37.8364,
              "x": 144.92214
            }
          },
          "Sections": {
            "Sec": [
              {
                "mode": 3,
                "id": "R0017c3-C0-S0",
                "Dep": {
                  "time": "2018-10-01T07:33:00",
                  "Stn": {
                    "y": -37.799157,
                    "x": 144.994451,
                    "name": "Victoria Park Railway Station (Abbotsford)",
                    "id": "721280150"
                  },
                  "Transport": {
                    "mode": 3,
                    "dir": "City (Flinders Street)",
                    "name": "Hurstbridge",
                    "At": {
                      "category": "Regional Train",
                      "color": "#EE2E24",
                      "textColor": "#FFFFFF",
                      "operator": "fup00100"
                    }
                  },
                  "Freq": {
                    "min": 3,
                    "max": 8,
                    "AltDep": [
                      {
                        "time": "2018-10-01T07:36:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:42:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:50:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      }
                    ]
                  }
                },
                "Journey": {
                  "duration": "PT13M",
                  "Stop": [
                    {
                      "dep": "2018-10-01T07:33:00",
                      "Stn": {
                        "y": -37.799157,
                        "x": 144.994451,
                        "name": "Victoria Park Railway Station (Abbotsford)",
                        "id": "721280150"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:34:00",
                      "Stn": {
                        "y": -37.804525,
                        "x": 144.993749,
                        "name": "Collingwood Railway Station (Abbotsford)",
                        "id": "721280151"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:36:00",
                      "Stn": {
                        "y": -37.810398,
                        "x": 144.9925,
                        "name": "North Richmond Railway Station (Richmond)",
                        "id": "721280152"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:37:00",
                      "Stn": {
                        "y": -37.814949,
                        "x": 144.991422,
                        "name": "West Richmond Railway Station (Richmond)",
                        "id": "721280153"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:39:00",
                      "Stn": {
                        "y": -37.816527,
                        "x": 144.984098,
                        "name": "Jolimont-Mcg Railway Station (East Melbourne)",
                        "id": "721280154"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:43:00",
                      "Stn": {
                        "y": -37.818305,
                        "x": 144.966964,
                        "name": "Flinders Street Railway Station (Melbourne City)",
                        "id": "721270081"
                      }
                    },
                    {
                      "arr": "2018-10-01T07:46:00",
                      "Stn": {
                        "y": -37.818334,
                        "x": 144.952524,
                        "name": "Southern Cross Railway Station (Melbourne City)",
                        "id": "721270006"
                      }
                    }
                  ]
                },
                "Arr": {
                  "time": "2018-10-01T07:46:00",
                  "Stn": {
                    "y": -37.818334,
                    "x": 144.952524,
                    "name": "Southern Cross Railway Station (Melbourne City)",
                    "id": "721270006"
                  }
                }
              },
              {
                "mode": 20,
                "id": "R0017c3-C0-S1",
                "Dep": {
                  "time": "2018-10-01T07:46:00",
                  "Stn": {
                    "y": -37.818334,
                    "x": 144.952524,
                    "name": "Southern Cross Railway Station (Melbourne City)",
                    "id": "721270006"
                  },
                  "Transport": {
                    "mode": 20
                  }
                },
                "Journey": {
                  "duration": "PT03M",
                  "distance": 137
                },
                "Arr": {
                  "time": "2018-10-01T07:49:00",
                  "Stn": {
                    "y": -37.817062,
                    "x": 144.953514,
                    "name": "1-Spencer St/Bourke St (Melbourne City)",
                    "id": "721290381"
                  }
                }
              },
              {
                "mode": 8,
                "id": "R0017c3-C0-S2",
                "Dep": {
                  "time": "2018-10-01T07:50:00",
                  "Stn": {
                    "y": -37.817062,
                    "x": 144.953514,
                    "name": "1-Spencer St/Bourke St (Melbourne City)",
                    "id": "721290381"
                  },
                  "Transport": {
                    "mode": 8,
                    "dir": "East Brunswick to St Kilda Beach",
                    "name": "96",
                    "At": {
                      "category": "Tram",
                      "color": "#CC2D98",
                      "textColor": "#FFFFFF",
                      "operator": "fuq00100"
                    }
                  },
                  "Freq": {
                    "min": 7,
                    "max": 8,
                    "AltDep": [
                      {
                        "time": "2018-10-01T07:57:00",
                        "Transport": {
                          "mode": 8,
                          "dir": "East Brunswick to St Kilda Beach",
                          "name": "96",
                          "At": {
                            "category": "Tram",
                            "color": "#CC2D98",
                            "textColor": "#FFFFFF",
                            "operator": "fuq00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T08:05:00",
                        "Transport": {
                          "mode": 8,
                          "dir": "East Brunswick to St Kilda Beach",
                          "name": "96",
                          "At": {
                            "category": "Tram",
                            "color": "#CC2D98",
                            "textColor": "#FFFFFF",
                            "operator": "fuq00100"
                          }
                        }
                      }
                    ]
                  }
                },
                "Journey": {
                  "duration": "PT03M",
                  "Stop": [
                    {
                      "dep": "2018-10-01T07:50:00",
                      "Stn": {
                        "y": -37.817062,
                        "x": 144.953514,
                        "name": "1-Spencer St/Bourke St (Melbourne City)",
                        "id": "721290381"
                      }
                    },
                    {
                      "arr": "2018-10-01T07:53:00",
                      "Stn": {
                        "y": -37.818775,
                        "x": 144.954069,
                        "name": "122-Southern Cross Railway Station/Spencer St (Melbourne City)",
                        "id": "721290580"
                      }
                    }
                  ]
                },
                "Arr": {
                  "time": "2018-10-01T07:53:00",
                  "Stn": {
                    "y": -37.818775,
                    "x": 144.954069,
                    "name": "122-Southern Cross Railway Station/Spencer St (Melbourne City)",
                    "id": "721290580"
                  }
                }
              },
              {
                "mode": 20,
                "id": "R0017c3-C0-S3",
                "Dep": {
                  "time": "2018-10-01T07:53:00",
                  "Stn": {
                    "y": -37.818775,
                    "x": 144.954069,
                    "name": "122-Southern Cross Railway Station/Spencer St (Melbourne City)",
                    "id": "721290580"
                  },
                  "Transport": {
                    "mode": 20
                  }
                },
                "Journey": {
                  "duration": "PT05M",
                  "distance": 62,
                  "_guide": 0
                },
                "Arr": {
                  "time": "2018-10-01T07:58:00",
                  "Stn": {
                    "y": -37.819274,
                    "x": 144.953748,
                    "name": "Southern Cross Station/Collins St (Melbourne City)",
                    "id": "422456480"
                  }
                }
              },
              {
                "mode": 5,
                "id": "R0017c3-C0-S4",
                "Dep": {
                  "time": "2018-10-01T07:58:00",
                  "Stn": {
                    "y": -37.819274,
                    "x": 144.953748,
                    "name": "Southern Cross Station/Collins St (Melbourne City)",
                    "id": "422456480"
                  },
                  "Transport": {
                    "mode": 5,
                    "dir": "Fishermans Bend",
                    "name": "235",
                    "At": {
                      "category": "Bus",
                      "color": "#FF8200",
                      "textColor": "#FFFFFF",
                      "operator": "fur00100"
                    }
                  },
                  "Freq": {
                    "min": 5,
                    "max": 8,
                    "AltDep": [
                      {
                        "time": "2018-10-01T08:06:00",
                        "Transport": {
                          "mode": 5,
                          "dir": "Fishermans Bend",
                          "name": "235",
                          "At": {
                            "category": "Bus",
                            "color": "#FF8200",
                            "textColor": "#FFFFFF",
                            "operator": "fur00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T08:11:00",
                        "Transport": {
                          "mode": 5,
                          "dir": "Fishermans Bend",
                          "name": "235",
                          "At": {
                            "category": "Bus",
                            "color": "#FF8200",
                            "textColor": "#FFFFFF",
                            "operator": "fur00100"
                          }
                        }
                      }
                    ]
                  }
                },
                "Journey": {
                  "duration": "PT16M",
                  "Stop": [
                    {
                      "dep": "2018-10-01T07:58:00",
                      "Stn": {
                        "y": -37.819274,
                        "x": 144.953748,
                        "name": "Southern Cross Station/Collins St (Melbourne City)",
                        "id": "422456480"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:07:00",
                      "Stn": {
                        "y": -37.829548,
                        "x": 144.947567,
                        "name": "Montague St/Normanby Rd (South Melbourne)",
                        "id": "422405037"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:08:00",
                      "Stn": {
                        "y": -37.831142,
                        "x": 144.944899,
                        "name": "Ingles St/Normanby Rd (Port Melbourne)",
                        "id": "422405038"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:09:00",
                      "Stn": {
                        "y": -37.831633,
                        "x": 144.942567,
                        "name": "Port Melbourne Cricket Ground/Williamstown Rd (Port Melbourne)",
                        "id": "422405039"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:09:00",
                      "Stn": {
                        "y": -37.83192,
                        "x": 144.940934,
                        "name": "Derham St/Williamstown Rd (Port Melbourne)",
                        "id": "422405040"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:10:00",
                      "Stn": {
                        "y": -37.832382,
                        "x": 144.939013,
                        "name": "Bridge St/Williamstown Rd (Port Melbourne)",
                        "id": "422405041"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:10:00",
                      "Stn": {
                        "y": -37.832729,
                        "x": 144.937219,
                        "name": "Farrell St/Williamstown Rd (Port Melbourne)",
                        "id": "422405042"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:11:00",
                      "Stn": {
                        "y": -37.833078,
                        "x": 144.935562,
                        "name": "Southward Ave/Williamstown Rd (Port Melbourne)",
                        "id": "422405043"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:13:00",
                      "Stn": {
                        "y": -37.834133,
                        "x": 144.929442,
                        "name": "Beacon Rd/Williamstown Rd (Port Melbourne)",
                        "id": "422405044"
                      }
                    },
                    {
                      "arr": "2018-10-01T08:14:00",
                      "Stn": {
                        "y": -37.834584,
                        "x": 144.92793,
                        "name": "Page Ave/Williamstown Rd (Port Melbourne)",
                        "id": "422405045"
                      }
                    }
                  ]
                },
                "Arr": {
                  "time": "2018-10-01T08:14:00",
                  "Stn": {
                    "y": -37.834584,
                    "x": 144.92793,
                    "name": "Page Ave/Williamstown Rd (Port Melbourne)",
                    "id": "422405045"
                  }
                }
              },
              {
                "mode": 20,
                "id": "R0017c3-C0-S5",
                "Dep": {
                  "time": "2018-10-01T08:14:00",
                  "Stn": {
                    "y": -37.834584,
                    "x": 144.92793,
                    "name": "Page Ave/Williamstown Rd (Port Melbourne)",
                    "id": "422405045"
                  },
                  "Transport": {
                    "mode": 20
                  }
                },
                "Journey": {
                  "duration": "PT10M",
                  "distance": 617
                },
                "Arr": {
                  "time": "2018-10-01T08:24:00",
                  "Addr": {
                    "y": -37.8364,
                    "x": 144.92214
                  }
                }
              }
            ]
          }
        },
        {
          "id": "R0017c3-C1",
          "duration": "PT01H04M",
          "transfers": 1,
          "Dep": {
            "time": "2018-10-01T07:33:00",
            "Stn": {
              "y": -37.799157,
              "x": 144.994451,
              "name": "Victoria Park Railway Station (Abbotsford)",
              "id": "721280150"
            }
          },
          "Arr": {
            "time": "2018-10-01T08:37:00",
            "Addr": {
              "y": -37.8364,
              "x": 144.92214
            }
          },
          "Sections": {
            "Sec": [
              {
                "mode": 3,
                "id": "R0017c3-C1-S0",
                "Dep": {
                  "time": "2018-10-01T07:33:00",
                  "Stn": {
                    "y": -37.799157,
                    "x": 144.994451,
                    "name": "Victoria Park Railway Station (Abbotsford)",
                    "id": "721280150"
                  },
                  "Transport": {
                    "mode": 3,
                    "dir": "City (Flinders Street)",
                    "name": "Hurstbridge",
                    "At": {
                      "category": "Regional Train",
                      "color": "#EE2E24",
                      "textColor": "#FFFFFF",
                      "operator": "fup00100"
                    }
                  },
                  "Freq": {
                    "min": 3,
                    "max": 8,
                    "AltDep": [
                      {
                        "time": "2018-10-01T07:36:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:42:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:50:00",
                        "Transport": {
                          "mode": 3,
                          "dir": "City (Flinders Street)",
                          "name": "South Morang",
                          "At": {
                            "category": "Regional Train",
                            "color": "#EE2E24",
                            "textColor": "#FFFFFF",
                            "operator": "fup00100"
                          }
                        }
                      }
                    ]
                  }
                },
                "Journey": {
                  "duration": "PT03M",
                  "Stop": [
                    {
                      "dep": "2018-10-01T07:33:00",
                      "Stn": {
                        "y": -37.799157,
                        "x": 144.994451,
                        "name": "Victoria Park Railway Station (Abbotsford)",
                        "id": "721280150"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:34:00",
                      "Stn": {
                        "y": -37.804525,
                        "x": 144.993749,
                        "name": "Collingwood Railway Station (Abbotsford)",
                        "id": "721280151"
                      }
                    },
                    {
                      "arr": "2018-10-01T07:36:00",
                      "Stn": {
                        "y": -37.810398,
                        "x": 144.9925,
                        "name": "North Richmond Railway Station (Richmond)",
                        "id": "721280152"
                      }
                    }
                  ]
                },
                "Arr": {
                  "time": "2018-10-01T07:36:00",
                  "Stn": {
                    "y": -37.810398,
                    "x": 144.9925,
                    "name": "North Richmond Railway Station (Richmond)",
                    "id": "721280152"
                  }
                }
              },
              {
                "mode": 20,
                "id": "R0017c3-C1-S1",
                "Dep": {
                  "time": "2018-10-01T07:36:00",
                  "Stn": {
                    "y": -37.810398,
                    "x": 144.9925,
                    "name": "North Richmond Railway Station (Richmond)",
                    "id": "721280152"
                  },
                  "Transport": {
                    "mode": 20
                  }
                },
                "Journey": {
                  "duration": "PT05M",
                  "distance": 203
                },
                "Arr": {
                  "time": "2018-10-01T07:41:00",
                  "Stn": {
                    "y": -37.80977,
                    "x": 144.991074,
                    "name": "18-Hoddle St/Victoria St (Richmond)",
                    "id": "721291447"
                  }
                }
              },
              {
                "mode": 8,
                "id": "R0017c3-C1-S2",
                "Dep": {
                  "time": "2018-10-01T07:42:00",
                  "Stn": {
                    "y": -37.80977,
                    "x": 144.991074,
                    "name": "18-Hoddle St/Victoria St (Richmond)",
                    "id": "721291447"
                  },
                  "Transport": {
                    "mode": 8,
                    "dir": "Box Hill to Port Melbourne",
                    "name": "109",
                    "At": {
                      "category": "Tram",
                      "color": "#00987E",
                      "textColor": "#FFFFFF",
                      "operator": "fuq00100"
                    }
                  },
                  "Freq": {
                    "min": 2,
                    "max": 8,
                    "AltDep": [
                      {
                        "time": "2018-10-01T07:44:00",
                        "Transport": {
                          "mode": 8,
                          "dir": "Box Hill to Port Melbourne",
                          "name": "109",
                          "At": {
                            "category": "Tram",
                            "color": "#00987E",
                            "textColor": "#FFFFFF",
                            "operator": "fuq00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:49:00",
                        "Transport": {
                          "mode": 8,
                          "dir": "Box Hill to Port Melbourne",
                          "name": "109",
                          "At": {
                            "category": "Tram",
                            "color": "#00987E",
                            "textColor": "#FFFFFF",
                            "operator": "fuq00100"
                          }
                        }
                      },
                      {
                        "time": "2018-10-01T07:57:00",
                        "Transport": {
                          "mode": 8,
                          "dir": "Box Hill to Port Melbourne",
                          "name": "109",
                          "At": {
                            "category": "Tram",
                            "color": "#00987E",
                            "textColor": "#FFFFFF",
                            "operator": "fuq00100"
                          }
                        }
                      }
                    ]
                  }
                },
                "Journey": {
                  "duration": "PT31M",
                  "Stop": [
                    {
                      "dep": "2018-10-01T07:42:00",
                      "Stn": {
                        "y": -37.80977,
                        "x": 144.991074,
                        "name": "18-Hoddle St/Victoria St (Richmond)",
                        "id": "721291447"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:42:00",
                      "Stn": {
                        "y": -37.809291,
                        "x": 144.985737,
                        "name": "16-Wellington St/Victoria Pde (East Melbourne)",
                        "id": "721291446"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:42:00",
                      "Stn": {
                        "y": -37.808978,
                        "x": 144.98269,
                        "name": "15-Smith St/Victoria Pde (East Melbourne)",
                        "id": "721291445"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:42:00",
                      "Stn": {
                        "y": -37.808551,
                        "x": 144.978806,
                        "name": "13-Lansdowne St/Victoria Pde (East Melbourne)",
                        "id": "721291444"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:47:00",
                      "Stn": {
                        "y": -37.808279,
                        "x": 144.976133,
                        "name": "12-St Vincents Plaza/Victoria Pde (East Melbourne)",
                        "id": "721290418"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:47:00",
                      "Stn": {
                        "y": -37.80939,
                        "x": 144.975728,
                        "name": "11-Albert St/Gisborne St (East Melbourne)",
                        "id": "721290419"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:47:00",
                      "Stn": {
                        "y": -37.81236,
                        "x": 144.974442,
                        "name": "10-Parliament Railway Station/Macarthur St (East Melbourne)",
                        "id": "721290420"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:51:00",
                      "Stn": {
                        "y": -37.813558,
                        "x": 144.973365,
                        "name": "8-Spring St/Collins St (Melbourne City)",
                        "id": "721290421"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:53:00",
                      "Stn": {
                        "y": -37.814436,
                        "x": 144.970478,
                        "name": "7-101 Collins St (Melbourne City)",
                        "id": "721291698"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:53:00",
                      "Stn": {
                        "y": -37.815695,
                        "x": 144.966127,
                        "name": "6-Melbourne Town Hall/Collins St (Melbourne City)",
                        "id": "721290422"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:56:00",
                      "Stn": {
                        "y": -37.816341,
                        "x": 144.96386,
                        "name": "5-Elizabeth St/Collins St (Melbourne City)",
                        "id": "721290423"
                      }
                    },
                    {
                      "dep": "2018-10-01T07:58:00",
                      "Stn": {
                        "y": -37.817607,
                        "x": 144.959429,
                        "name": "3-William St/Collins St (Melbourne City)",
                        "id": "721291728"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:01:00",
                      "Stn": {
                        "y": -37.818881,
                        "x": 144.954997,
                        "name": "1-Spencer St/Collins St (Melbourne City)",
                        "id": "721290426"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:03:00",
                      "Stn": {
                        "y": -37.822,
                        "x": 144.955525,
                        "name": "124-Batman Park/Spencer St (Melbourne City)",
                        "id": "721291880"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:03:00",
                      "Stn": {
                        "y": -37.82359,
                        "x": 144.95631,
                        "name": "124A-Casino/Mcec/Clarendon St (Southbank)",
                        "id": "721290583"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:07:00",
                      "Stn": {
                        "y": -37.826076,
                        "x": 144.956207,
                        "name": "125-Port Junction/79 Whiteman St (Southbank)",
                        "id": "721291798"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:08:00",
                      "Stn": {
                        "y": -37.827691,
                        "x": 144.953709,
                        "name": "125A-Southbank Tram Depot/Light Rail (South Melbourne)",
                        "id": "721291439"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:09:00",
                      "Stn": {
                        "y": -37.830244,
                        "x": 144.948696,
                        "name": "126-Montague St/Light Rail (South Melbourne)",
                        "id": "721291440"
                      }
                    },
                    {
                      "dep": "2018-10-01T08:11:00",
                      "Stn": {
                        "y": -37.833431,
                        "x": 144.943335,
                        "name": "127-North Port Station/Light Rail (Port Melbourne)",
                        "id": "721291441"
                      }
                    },
                    {
                      "arr": "2018-10-01T08:13:00",
                      "Stn": {
                        "y": -37.837032,
                        "x": 144.937463,
                        "name": "128-Graham St/Light Rail (Port Melbourne)",
                        "id": "721291442"
                      }
                    }
                  ]
                },
                "Arr": {
                  "time": "2018-10-01T08:13:00",
                  "Stn": {
                    "y": -37.837032,
                    "x": 144.937463,
                    "name": "128-Graham St/Light Rail (Port Melbourne)",
                    "id": "721291442"
                  }
                }
              },
              {
                "mode": 20,
                "id": "R0017c3-C1-S3",
                "Dep": {
                  "time": "2018-10-01T08:13:00",
                  "Stn": {
                    "y": -37.837032,
                    "x": 144.937463,
                    "name": "128-Graham St/Light Rail (Port Melbourne)",
                    "id": "721291442"
                  },
                  "Transport": {
                    "mode": 20
                  }
                },
                "Journey": {
                  "duration": "PT24M",
                  "distance": 1435
                },
                "Arr": {
                  "time": "2018-10-01T08:37:00",
                  "Addr": {
                    "y": -37.8364,
                    "x": 144.92214
                  }
                }
              }
            ]
          }
        }
      ],
      "Operators": {
        "Op": [
          {
            "code": "fur00100",
            "type": "TT",
            "name": "PTV",
            "short_name": "1",
            "Link": [
              {
                "type": "website",
                "href": "https://transit.api.here.com/r?appId=cZRxcNsM5dufeS7fWZes&u=http://www.ptv.vic.gov.au",
                "sec_ids": "R0017c3-C0-S4",
                "text": "PTV"
              }
            ]
          },
          {
            "code": "fup00100",
            "type": "TT",
            "name": "PTV",
            "short_name": "1",
            "Link": [
              {
                "type": "website",
                "href": "https://transit.api.here.com/r?appId=cZRxcNsM5dufeS7fWZes&u=http://www.ptv.vic.gov.au",
                "sec_ids": "R0017c3-C0-S0 R0017c3-C1-S0",
                "text": "PTV"
              }
            ]
          },
          {
            "code": "fuq00100",
            "type": "TT",
            "name": "PTV",
            "short_name": "1",
            "Link": [
              {
                "type": "website",
                "href": "https://transit.api.here.com/r?appId=cZRxcNsM5dufeS7fWZes&u=http://www.ptv.vic.gov.au",
                "sec_ids": "R0017c3-C0-S2 R0017c3-C1-S2",
                "text": "PTV"
              }
            ]
          }
        ]
      }
    },
    "Guidance": {
      "Maneuvers": [
        {
          "sec_ids": "R0017c3-C0-S1",
          "Maneuver": [
            {
              "direction": "forward",
              "action": "depart",
              "distance": 108,
              "duration": "PT0H1M57S",
              "next_road": "Spencer St",
              "next_number": "50",
              "instruction": "Head north on Spencer St. Go for 108 m.",
              "graph": "-37.8180635,144.9535918 -37.8174734,144.9533343 -37.8171408,144.9531949"
            },
            {
              "direction": "right",
              "action": "rightTurn",
              "distance": 29,
              "duration": "PT0H0M29S",
              "next_road": "Bourke St",
              "instruction": "Turn right onto Bourke St. Go for 29 m.",
              "graph": "-37.8171408,144.9531949 -37.8170764,144.9533558 -37.8170297,144.9534963"
            },
            {
              "direction": "forward",
              "action": "arrive",
              "distance": 0,
              "duration": "PT0H0M0S",
              "instruction": "Arrive at Bourke St. Your destination is on the right.",
              "graph": "-37.8170297,144.9534963"
            }
          ]
        },
        {
          "sec_ids": "R0017c3-C0-S5",
          "Maneuver": [
            {
              "direction": "forward",
              "action": "depart",
              "distance": 528,
              "duration": "PT0H8M48S",
              "next_road": "Williamstown Rd",
              "instruction": "Head west on Williamstown Rd. Go for 528 m.",
              "graph": "-37.8344838,144.9278987 -37.8345537,144.9275315 -37.8347576,144.9265015 -37.8348327,144.9261153 -37.8349292,144.9255788 -37.8350472,144.9249995 -37.8350687,144.9249029 -37.8351223,144.9246347 -37.8351653,144.9243987 -37.8352296,144.9240661 -37.835412,144.9231005 -37.8355193,144.922564 -37.8356159,144.9220705"
            },
            {
              "direction": "left",
              "action": "leftTurn",
              "distance": 89,
              "duration": "PT0H1M32S",
              "next_road": "Leith Cres",
              "instruction": "Turn left onto Leith Cres. Go for 89 m.",
              "graph": "-37.8356159,144.9220705 -37.8357446,144.922092 -37.8358948,144.9221027 -37.8361738,144.9221885 -37.8364098,144.9221349"
            },
            {
              "direction": "forward",
              "action": "arrive",
              "distance": 0,
              "duration": "PT0H0M0S",
              "instruction": "Arrive at Leith Cres.",
              "graph": "-37.8364098,144.9221349"
            }
          ]
        },
        {
          "sec_ids": "R0017c3-C1-S1",
          "Maneuver": [
            {
              "direction": "forward",
              "action": "depart",
              "distance": 73,
              "duration": "PT0H1M23S",
              "instruction": "Head north. Go for 73 m.",
              "graph": "-37.8103844,144.9923847 -37.8097379,144.9925053"
            },
            {
              "direction": "left",
              "action": "leftTurn",
              "distance": 112,
              "duration": "PT0H2M3S",
              "next_road": "Victoria St",
              "next_number": "32",
              "instruction": "Turn left onto Victoria St. Go for 112 m.",
              "graph": "-37.8097379,144.9925053 -37.8097272,144.9923658 -37.8096843,144.9922049 -37.8096414,144.9919045 -37.8095984,144.9914646 -37.8096199,144.99125"
            },
            {
              "direction": "left",
              "action": "leftTurn",
              "distance": 18,
              "duration": "PT0H0M18S",
              "instruction": "Turn left. Go for 18 m.",
              "graph": "-37.8096199,144.99125 -37.8097848,144.9912233"
            },
            {
              "direction": "forward",
              "action": "arrive",
              "distance": 0,
              "duration": "PT0H0M0S",
              "instruction": "Arrive at Hoddle St. Your destination is on the right.",
              "graph": "-37.8097848,144.9912233"
            }
          ]
        },
        {
          "sec_ids": "R0017c3-C1-S3",
          "Maneuver": [
            {
              "direction": "forward",
              "action": "depart",
              "distance": 11,
              "duration": "PT0H0M11S",
              "instruction": "Head southwest. Go for 11 m.",
              "graph": "-37.8369141,144.9372519 -37.8369892,144.937166"
            },
            {
              "direction": "lightLeft",
              "action": "slightLeftTurn",
              "distance": 106,
              "duration": "PT0H1M46S",
              "instruction": "Turn slightly left. Go for 106 m.",
              "graph": "-37.8369892,144.937166 -37.8370321,144.9371231 -37.8372145,144.9368119 -37.8375793,144.9362326"
            },
            {
              "direction": "right",
              "action": "rightTurn",
              "distance": 764,
              "duration": "PT0H12M58S",
              "instruction": "Turn right. Go for 764 m.",
              "graph": "-37.8375793,144.9362326 -37.8374398,144.9359751 -37.8374398,144.9358571 -37.8373861,144.935739 -37.8373647,144.9356747 -37.8373754,144.9355888 -37.8373647,144.9355245 -37.8372788,144.9351919 -37.8372467,144.9349773 -37.8372359,144.9347305 -37.8372467,144.9345052 -37.8372681,144.9343765 -37.8373754,144.9339688 -37.8375578,144.9335289 -37.83759,144.9333787 -37.8374076,144.9321127 -37.8373754,144.9319947 -37.8372896,144.9318445 -37.8372359,144.9316943 -37.8371823,144.931426 -37.8371716,144.9311149 -37.837193,144.9305677 -37.8369248,144.9284542 -37.8369784,144.9282074 -37.8370428,144.9282181 -37.8370535,144.9281967 -37.8370428,144.928025 -37.8369892,144.9279714"
            },
            {
              "direction": "right",
              "action": "rightTurn",
              "distance": 19,
              "duration": "PT0H0M34S",
              "next_road": "Beacon Rd",
              "instruction": "Turn right onto Beacon Rd. Go for 19 m.",
              "graph": "-37.8369892,144.9279714 -37.8369355,144.9281752"
            },
            {
              "direction": "left",
              "action": "leftRoundaboutExit1",
              "distance": 535,
              "duration": "PT0H8M59S",
              "next_road": "Howe Pde",
              "instruction": "Walk left around the roundabout and turn at the 1st street Howe Pde. Go for 535 m.",
              "graph": "-37.8369355,144.9281752 -37.8368926,144.9280894 -37.8367853,144.9267912 -37.8366566,144.9250531 -37.8365278,144.9235082 -37.8364098,144.9221349"
            },
            {
              "direction": "forward",
              "action": "arrive",
              "distance": 0,
              "duration": "PT0H0M0S",
              "instruction": "Arrive at Howe Pde.",
              "graph": "-37.8364098,144.9221349"
            }
          ]
        }
      ]
    }
  }
}
        """.trimIndent()

        assertNotNull(parseResponseJson(json))

    }

    @After
    fun tearDown() {
    }
}