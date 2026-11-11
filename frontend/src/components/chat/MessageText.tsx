import { Fragment } from "react"

const URL_PATTERN = /(https?:\/\/[^\s]+|www\.[^\s]+)/g
const URL_PREFIX = /^(https?:\/\/|www\.)/i
const TRAILING_PUNCTUATION = /[.,!?;:'")\]}]+$/

function linkify(text: string) {
  return text.split(URL_PATTERN).map((part, i) => {
    if (!URL_PREFIX.test(part)) return part
    const trailingMatch = part.match(TRAILING_PUNCTUATION)
    const trailing = trailingMatch ? trailingMatch[0] : ""
    const url = trailing ? part.slice(0, -trailing.length) : part
    const href = url.startsWith("www.") ? `https://${url}` : url
    return (
      <Fragment key={i}>
        <a href={href} target="_blank" rel="noopener noreferrer" className="underline underline-offset-2 hover:opacity-80">
          {url}
        </a>
        {trailing}
      </Fragment>
    )
  })
}

export function MessageText({ text }: { text: string }) {
  return <p className="whitespace-pre-wrap break-words">{linkify(text)}</p>
}
