path "secret/app/*" {
  policy = "write"
}

path "secret/app/foo" {
  policy = "read"
}


path "secret-v2/data/app/*" {
  policy = "write"
}

path "secret-V2/data/app/foo" {
  policy = "read"
}

path "auth/token/lookup-self" {
  policy = "read"
}

