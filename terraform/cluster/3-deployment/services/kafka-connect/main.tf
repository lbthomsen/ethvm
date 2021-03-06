resource "kubernetes_service" "kafka_connect_service" {
  metadata {
    name = "kafka-connect"
    namespace = "${var.namespace}"

    labels {
      app = "kafka-connect"
    }
  }

  spec {
    selector {
      app = "kafka-connect"
    }

    port {
      port     = 8083
      protocol = "TCP"
    }
  }
}

resource "kubernetes_stateful_set" "kafka_connect_stateful_set" {
  metadata {
    name = "kafka-connect"
    namespace = "${var.namespace}"

    labels {
      app = "kafka-connect"
    }
  }

  spec {
    selector {
      app = "kafka-connect"
    }

    replicas     = 1
    service_name = "kafka-connect"

    update_strategy {
      type = "RollingUpdate"
    }

    template {
      metadata {
        labels {
          app = "kafka-connect"
        }
      }

      spec {
        container {
          name              = "kafka-connect"
          image             = "enkryptio/kafka-connect:${var.kafka_connect_version}"
          image_pull_policy = "IfNotPresent"

          resources {
            requests {
              memory = "4Gi"
            }

            limits {
              memory = "5Gi"
            }
          }

          port {
            container_port = 8083
          }

          volume_mount {
            name       = "data"
            mount_path = "/kafka/connect"
          }

          readiness_probe {
            initial_delay_seconds = 60
            period_seconds        = 10
            timeout_seconds       = 5
            success_threshold     = 1
            failure_threshold     = 3

            tcp_socket {
              port = 9092
            }
          }

          liveness_probe {
            initial_delay_seconds = 60
            period_seconds        = 10
            timeout_seconds       = 5
            success_threshold     = 1
            failure_threshold     = 3

            tcp_socket {
              port = 9092
            }
          }

          env {
            name  = "KAFKA_JVM_PERFORMANCE_OPTS"
            value = "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
          }

          env {
            name  = "KAFKA_HEAP_OPTS"
            value = "-Xmx4G -Xms4G"
          }

          env {
            name  = "CONNECT_BOOTSTRAP_SERVERS"
            value = "kafka-headless:9092"
          }

          env {
            name  = "CONNECT_REST_ADVERTISED_HOST_NAME"
            value = "kafka-connect"
          }

          env {
            name  = "CONNECT_REST_PORT"
            value = "8083"
          }
        }
      }
    }

    volume_claim_templates {
      metadata {
        name = "data"
      }

      spec {
        storage_class_name = "${var.kafka_connect_storage_type}"
        access_modes       = ["ReadWriteOnce"]

        resources {
          requests {
            storage = "${var.kafka_connect_storage_size}"
          }
        }
      }
    }
  }
}
