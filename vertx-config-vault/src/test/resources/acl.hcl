path "secret/app/*" {
  policy = "write"
}

path "secret/app/foo" {
  policy = "read"
}

path "auth/token/lookup-self" {
  policy = "read"
}
