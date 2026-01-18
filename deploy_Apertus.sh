gcloud auth login
gcloud config set project apertus-484621
gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com

gcloud artifacts repositories create llm-repo \
  --repository-format=docker \
  --location=europe-west6

gcloud builds submit \
  --tag europe-west6-docker.pkg.dev/apertus-484621/llm-repo/apertus-vllm:latest


gcloud secrets add-iam-policy-binding hf-token \
  --member="serviceAccount:220823746257-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud run deploy apertus-vllm \
  --image europe-west6-docker.pkg.dev/apertus-484621/llm-repo/apertus-vllm:latest \
  --region europe-west1 \
  --allow-unauthenticated \
  --cpu 8 \
  --memory 32Gi \
  --gpu 1 \
  --port 8080 \
  --timeout 3600 \
  --concurrency 1 \
  --set-secrets=HUGGING_FACE_HUB_TOKEN=hf-token:latest

  
  
  
  curl -s https://YOUR_RUN_URL/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "swiss-ai/Apertus-8B-Instruct-2509",
    "messages": [
      {"role":"system","content":"You are a helpful assistant."},
      {"role":"user","content":"Say hi in Swiss German."}
    ],
    "temperature": 0.7
  }'
  
  
  
  
  
printf "%s" "-------------HUGGING FACE TOEK TO BE ADDED ----------" | gcloud secrets create hf-token --data-file=-